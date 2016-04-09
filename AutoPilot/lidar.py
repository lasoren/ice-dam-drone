from smbus import SMBus
import Adafruit_GPIO.FT232H as FT232H
import time, threading

class Lidar(threading.Thread):
    __DEF_ADDR = 0x62

    # register addresses
    __CONTROL_REG = 0x00
    __STATUS_REG = 0x01
    __AQUISIT_REG_MSB = 0x0f
    __AQUISIT_REG_LSB = 0x10
    __SERIAL_REG = 0x96

    # commands
    __RESET_FPGA = 0x00
    __AQUISIT_DC = 0x04
    __AQUISIT_NODC = 0x03

    # status register bits
    __MEASURE_ERROR = 0x40
    __DEVICE_OKAY = 0x20
    __INVALID_SIGNAL = 0x08
    __DEVICE_READY = 0x01

    distance = -1

    def __init__(self):
        super(Lidar, self).__init__()
        self.lock = threading.RLock()
        FT232H.use_FT232H()
        ft232h = FT232H.FT232H()
        self.bus = FT232H.I2CDevice(ft232h, self.__DEF_ADDR)
        if self.bus.ping():
            print "Connected"
        self.bus.write8(self.__CONTROL_REG, self.__RESET_FPGA)
        time.sleep(0.02)
        self._stop = threading.Event()
        self.start()

    def getDeviceStatus(self):
        return self.bus.readU8(self.__STATUS_REG)

    def isOkay(self):
        if (self.getDeviceStatus() & self.__DEVICE_OKAY) != self.__DEVICE_OKAY:
            return False
        else:
            return True

    def isReady(self):
        if (self.getDeviceStatus() & self.__DEVICE_READY) == 0:
            return True
        else:
            return False

    def checkMeasureError(self):
        if (self.getDeviceStatus() & self.__MEASURE_ERROR):
            return True
        else:
            return False

    def readDistance(self):
        return self.distance

    def run(self):
        self._stop.clear()
        while self._stop.isSet() is False:
            self.lock.acquire()
            self.distance = self.__getDistance()
            self.lock.release()
            time.sleep(0.02)

    def stop(self):
        self._stop.set()

    def acquireLock(self):
        self.lock.acquire()

    def releaseLock(self):
        self.lock.release()

    def __getDistance(self):
        self.__spin_while_not_ready()
        self.bus.write8(self.__CONTROL_REG, self.__AQUISIT_DC)
        self.__spin_while_not_ready()
        return (self.bus.readU8(self.__AQUISIT_REG_MSB) << 8) | (self.bus.readU8(self.__AQUISIT_REG_LSB))

    def __spin_while_not_ready(self):
        while (not self.isReady()):
            continue