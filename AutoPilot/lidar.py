from smbus import SMBus
from datetime import datetime
import time

class Lidar:
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

    def __init__(self):
        self.bus = SMBus(1)
        self.bus.write_byte_data(self.__DEF_ADDR, self.__CONTROL_REG, self.__RESET_FPGA)
        time.sleep(0.02)

    def getDeviceStatus(self):
        return self.bus.read_byte_data(self.__DEF_ADDR, self.__STATUS_REG)

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

    def getDistance(self):
        self.__spin_while_not_ready()
        self.bus.write_byte_data(self.__DEF_ADDR, self.__CONTROL_REG, self.__AQUISIT_DC)
        self.__spin_while_not_ready()
        return (self.bus.read_byte_data(self.__DEF_ADDR, self.__AQUISIT_REG_MSB) << 8) | (self.bus.read_byte_data(self.__DEF_ADDR, self.__AQUISIT_REG_LSB))

    def __spin_while_not_ready(self):
        while (not self.isReady()):
            continue

if __name__ == "__main__":
    sensor = Lidar()

    print "Sensor is a-go"
    for i in range(0, 99):
        distance = sensor.getDistance()
        if distance > 200:
            print "Incorrect distance measured %d\n" % distance
            if sensor.checkMeasureError():
                print "There was a measurement error\n"
        else:
            print "Measured distance %d\n" % distance