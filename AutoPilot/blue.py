from bluetooth import *
import EventHandler, threading, glob, errno, socket, detect_ice
import os
from house import *
from dronekit import LocationGlobalRelative

COMMAND_ARM = 0x1
COMMAND_UNARM = 0x2
COMMAND_START_INSPECTION = 0x3
COMMAND_END_INSPECTION = 0x4
COMMAND_SEND_STATUS = 0x5
COMMAND_SEND_POINTS = 0x6
COMMAND_READY_TO_TRANSFER = 0x7
COMMAND_NEW_HOUSE = 0x8
COMMAND_BLUETOOTH_SEND_PATH = 0x9
COMMAND_BLUETOOTH_SEND_IMAGES_RGB = 0xA
COMMAND_BLUETOOTH_SEND_IMAGES_THERM = 0xB
COMMAND_BLUETOOTH_RETURN_HOME = 0xC
COMMAND_BLUETOOTH_SEND_JSON_RGB = 0xD
COMMAND_BLUETOOTH_SEND_JSON_THERM = 0xE
COMMAND_BLUETOOTH_SEND_DRONE_LANDED = 0xF
COMMAND_BLUETOOTH_SEND_FINISHED_DAM = 0x10
COMMAND_BLUETOOTH_SEND_FINISHED_ALL_DAMS = 0x11
COMMAND_BLUETOOTH_SEND_LOW_BATTERY = 0x12
COMMAND_BLUETOOTH_SEND_ROOF_SCAN_INTERRUPTED = 0X13
COMMAND_BLUETOOTH_SEND_BORDER_SCAN_INTERRUPTED = 0X14
COMMAND_BLUETOOTH_SEND_FINISHED_SCAN = 0X15
COMMAND_BLUETOOTH_SEND_FINISH_BORDER = 0x16
COMMAND_BLUETOOTH_SEND_FINISH_ANALYSIS = 0x17
COMMAND_BLUETOOTH_FINISHED_RGB = 0x18
COMMAND_BLUETOOTH_FINISHED_THERM = 0x19

COMMAND_BLUETOOTH_OKAY_TO_SEND = 0x31
COMMAND_BLUETOOTH_SEND_CORRUPT = 0x30

class Blue(threading.Thread):
    __uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    __name = "girodicer-minnow"
    __client_sock = None

    def __init__(self, queue, debug=False):
        """
        Initializes the bluetooth system and will start itself as a thread
        :param queue: Main Event Queue
        """
        super(Blue, self).__init__()
        # set bluetooth to be in visible and pairing mode
        os.system('hciconfig hci0 piscan')
        self.queue = queue
        self.__stop = threading.Event()
        self.lock = threading.RLock()
        self.old_data = None
        self.write_okay = True
        if not debug:
            self.__server_sock = BluetoothSocket(RFCOMM)
            self.__server_sock.bind(("", PORT_ANY))
            self.__server_sock.listen(1)

            advertise_service(self.__server_sock, self.__name,
                              service_id=self.__uuid,
                              service_classes=[self.__uuid, SERIAL_PORT_CLASS],
                              profiles=[SERIAL_PORT_PROFILE])
            self.start()

    def run(self):
        self.__stop.clear()
        while self.__stop.isSet() is False:
            try:
                print "waiting for connection"
                self.__client_sock, client_info = self.__server_sock.accept()
                self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_CONNECTED)

                while self.__stop.isSet() is False:
                    try:
                        data = self.__client_sock.recv(1024)
                        BlueDataProcessor(data, self.queue, self)
                    except BluetoothError as error:
                        if error.message != "timed out":
                            if self.__client_sock is not None:
                                self.__client_sock.close()
                                self.__client_sock = None
                                self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.ERROR_BLUETOOTH_DISCONNECTED)
                        break
            except BluetoothError as error:
                if error.message != "timed out":
                    raise error

        if self.__client_sock is not None:
            self.__client_sock.close()
        if self.__server_sock is not None:
            self.__server_sock.close()
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.ERROR_BLUETOOTH_DISCONNECTED)
        print "Bluetooth Stopped"

    def write(self, data):
        if self.__client_sock is not None:
            while True:
                if self.write_okay is True:
                    try:
                        self.__client_sock.send(data)
                        self.old_data = data
                        self.write_okay = False
                        break
                        # print ":".join(x.encode('hex') for x in data[0:2])
                    except BluetoothError as error:
                        if (error.message != "(107, 'Transport endpoint is not connected')"):
                            raise error
                        break

    def rewrite(self):
        if self.__client_sock is not None:
            if (self.old_data is not None) and (self.write_okay is False):
                try:
                    self.__client_sock.send(self.old_data)
                except BluetoothError as error:
                        if (error.message != "(107, 'Transport endpoint is not connected')"):
                            raise error


    def getlock(self):
        self.lock.acquire()

    def unlock(self):
        self.lock.release()

    def stop(self):
        self.__stop.set()

class BlueDataProcessor(threading.Thread):

    def __init__(self, data, queue, bluetooth):
        super(BlueDataProcessor, self).__init__()
        self.data = data
        self.queue = queue
        self.bluetooth = bluetooth
        self.start()

    def run(self):
        # print ":".join(x.encode('hex') for x in self.data)
        (command, payloadSize) = struct.unpack_from('<Bi', self.data)

        if command == COMMAND_ARM:
            None
        elif command == COMMAND_UNARM:
            None
        elif command == COMMAND_START_INSPECTION:
            self.__start_inspection()
        elif command == COMMAND_END_INSPECTION:
            self.__end_inspection()
        elif command == COMMAND_SEND_STATUS:
            self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_SEND_STATUS)
        elif command == COMMAND_SEND_POINTS:
            self.__decipherRcvdPoints(payloadSize)
        elif command == COMMAND_NEW_HOUSE:
            print "new house"
            print "payloadsize %d" % payloadSize
            path = self.__calculatePath(payloadSize)
            packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_PATH, path, self.bluetooth)
            packager.run()
        elif command == COMMAND_BLUETOOTH_RETURN_HOME:
            self.__return_home()
        elif command == COMMAND_BLUETOOTH_SEND_IMAGES_RGB:
            print "Transferring RGB Images"
            self.__packImages_rgb()
        elif command == COMMAND_BLUETOOTH_SEND_IMAGES_THERM:
            print "Transferring THERMAL Images"
            self.__packImages_therm()
        elif command == COMMAND_BLUETOOTH_SEND_CORRUPT:
            self.bluetooth.rewrite()
        elif command == COMMAND_BLUETOOTH_OKAY_TO_SEND:
            self.bluetooth.write_okay = True

    def __decipherRcvdPoints(self, payloadSize):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_GET_POINTS, self.__unpackagePoints(payloadSize))

    def __unpackagePoints(self, payloadSize):
        offset = struct.calcsize('<Bi')
        windPos = struct.calcsize('<d')
        numPoints = (payloadSize/windPos)/2

        points = []

        for i in range(0, numPoints):
            (lat,) = struct.unpack_from('>d', self.data, offset)
            offset += windPos
            (lng, ) = struct.unpack_from('>d', self.data, offset)
            print "%f,%f" % (lat,lng)
            points.append(LocationGlobalRelative(lat,lng))
            offset += windPos

        return points

    def __calculatePath(self, payloadSize):
        newhouse = house(self.__unpackagePoints(payloadSize))
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_NEW_HOUSE, [newhouse])
        return newhouse.path

    def __start_inspection(self):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.START_SCAN)

    def __end_inspection(self):
        self.queue.add(EventHandler.HIGH_PRIORITY, EventHandler.RETURN_TO_LAUNCH)

    def __packImages_rgb(self):
        self.bluetooth.getlock()
        rgb_img_dir = os.path.join(os.path.expanduser('~'), 'ice-dam-drone', 'images', 'rgb_proc')
        os.chdir(rgb_img_dir)
        self.__sendjson_rgb()
        img_list = [str(file) for file in detect_ice.nat_sort(glob.glob("*.jpg"))]
        print "Transferring RGB Images..."
        for i in range(0, len(img_list)):
            print "Sending Image %d" % i
            img_path = img_list[i]
            with open(img_path, "rb") as imageFile:
                f_img = imageFile.read()
                packets = int(math.ceil(len(f_img)/512))
                for j in range(1, packets+1):
                    sub = f_img[(j-1)*512:512*j]
                    img_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_IMAGES_RGB, sub, self.bluetooth, flag=0x80)
                    img_packager.run()
                sub = f_img[(packets)*512:512*(packets+1)]
                img_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_IMAGES_RGB, sub, self.bluetooth, flag=0xC0)
                img_packager.run()
        self.bluetooth.unlock()
        BlueDataPackager(COMMAND_BLUETOOTH_FINISHED_RGB, 0, self.bluetooth)

    def __packImages_therm(self):
        self.bluetooth.getlock()
        therm_img_dir = os.path.join(os.path.expanduser('~'), 'ice-dam-drone', 'images', 'thermal_proc')
        os.chdir(therm_img_dir)
        self.__sendjson_therm()
        img_list = [str(file) for file in detect_ice.nat_sort(glob.glob("*.jpg"))]
        for i in range(0, len(img_list)):
            print "Sending Image %d" % i
            img_path = img_list[i]
            with open(img_path, "rb") as imageFile:
                f_img = imageFile.read()
                packets = int(math.ceil(len(f_img)/512))
                for j in range(1, packets+1):
                    sub = f_img[(j-1)*512:512*j]
                    img_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_IMAGES_THERM, sub, self.bluetooth, flag=0x80)
                    img_packager.run()
                sub = f_img[(packets)*512:512*(packets+1)]
                img_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_IMAGES_THERM, sub, self.bluetooth, flag=0xC0)
                img_packager.run()
        self.bluetooth.unlock()
        BlueDataPackager(COMMAND_BLUETOOTH_FINISHED_THERM, 0, self.bluetooth)

    def __sendjson_rgb(self):
        with open('images.json', 'r') as jsonFile:
            print "Transferring RGB JSON packets..."
            f_json = jsonFile.read()
            packets = int(math.ceil(len(f_json)/512))
            for i in range(1, packets+1):
                sub = f_json[((i-1)*512):512*i]
                json_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_JSON_RGB, sub, self.bluetooth, flag=0x80)
                json_packager.run()
            sub = f_json[(packets)*512:512*(packets+1)]
            print sub
            json_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_JSON_RGB, sub, self.bluetooth, flag=0xC0)
            json_packager.run()
            print "Finished transferring JSON data!!!"

    def __sendjson_therm(self):
        with open('images.json', 'r') as jsonFile:
            print "Transferring THERMAL JSON packets..."
            f_json = jsonFile.read()
            packets = int(math.ceil(len(f_json)/512))
            for i in range(1, packets+1):
                sub = f_json[((i-1)*512):512*i]
                json_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_JSON_THERM, sub, self.bluetooth, flag=0x80)
                json_packager.run()
            sub = f_json[(packets)*512:512*(packets+1)]
            print sub
            json_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_JSON_THERM, sub, self.bluetooth, flag=0xC0)
            json_packager.run()
            print "Finished transferring JSON data!!!"

    def __return_home(self):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.RETURN_TO_LAUNCH)


class BlueDataPackager(threading.Thread):

    def __init__(self, command, payload, bluetooth, flag=0x0):
        super(BlueDataPackager, self).__init__()
        self.command = command
        self.payload = payload
        self.bluetooth = bluetooth
        self.flag = flag

    def run(self):
        if self.command == COMMAND_BLUETOOTH_SEND_PATH:
            self.__sendPath()
        elif self.command == COMMAND_SEND_STATUS:
            self.__sendStatus()
        elif self.command == COMMAND_BLUETOOTH_SEND_IMAGES_RGB or self.command == COMMAND_BLUETOOTH_SEND_IMAGES_THERM:
            self.__sendImage()
        elif self.command == COMMAND_BLUETOOTH_SEND_JSON_RGB or COMMAND_BLUETOOTH_SEND_JSON_THERM:
            self.__sendJson()
        elif (self.command == COMMAND_BLUETOOTH_SEND_DRONE_LANDED or COMMAND_BLUETOOTH_SEND_FINISHED_ALL_DAMS
              or COMMAND_BLUETOOTH_SEND_FINISHED_DAM or COMMAND_BLUETOOTH_SEND_LOW_BATTERY
              or COMMAND_BLUETOOTH_SEND_ROOF_SCAN_INTERRUPTED or COMMAND_BLUETOOTH_SEND_BORDER_SCAN_INTERRUPTED
              or COMMAND_BLUETOOTH_SEND_FINISHED_SCAN):
            self.__send_nopayload()

    def __sendStatus(self):
        payloadSize = struct.calcsize('>ffdBi')

        data = struct.pack('>BiffdBi', self.command|self.flag, payloadSize, self.payload[0], self.payload[1], self.payload[2],
                           self.payload[3], self.payload[4])

        self.bluetooth.getlock()
        self.bluetooth.write(data)
        self.bluetooth.unlock()

    def __sendPath(self):
        """

        :return:
        """
        numPoints = len(self.payload)
        pointSize = struct.calcsize('>dd')
        payloadSize = numPoints * pointSize

        data = struct.pack('>Bi', self.command|self.flag, payloadSize)

        for i in range(0, numPoints):
            data = ''.join([data, struct.pack('>dd', self.payload[i].lat, self.payload[i].lon)])

        self.bluetooth.write(data)

    def __sendImage(self):
        payloadSize = len(self.payload)

        data = struct.pack('>Bi', self.command|self.flag, payloadSize)
        data = ''.join([data, self.payload])
        print "image packet size %d" % len(data)

        self.bluetooth.write(data)

    def __sendJson(self):
        payloadSize = len(self.payload)

        data = struct.pack('>Bi', self.command|self.flag, payloadSize)
        data = ''.join([data, self.payload])
        print "json packet size %d" % len(data)

        self.bluetooth.write(data)

    def __send_nopayload(self):
        payloadSize = 0
        data = struct.pack('>Bi', self.command|self.flag, payloadSize)

        print "sending notification"
        print ":".join(x.encode('hex') for x in self.data)
        self.bluetooth.getlock()
        self.bluetooth.write(data)
        self.bluetooth.unlock()