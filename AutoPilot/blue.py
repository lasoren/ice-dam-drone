from bluetooth import *
import EventHandler, threading, glob
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

class Blue(threading.Thread):
    __uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    __name = "ubuntu-mate-0"
    __client_sock = None

    def __init__(self, queue, debug=False):
        """
        Initializes the bluetooth system and will start itself as a thread
        :param queue: Main Event Queue
        """
        super(Blue, self).__init__()
        self.queue = queue
        self.__stop = threading.Event()
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
            self.__client_sock, client_info = self.__server_sock.accept()
            self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_CONNECTED)
            try:
                while self.__stop.isSet() is False:
                    data = self.__client_sock.recv(1024)
                    BlueDataProcessor(data, self.queue, self)
            except IOError:
                self.__client_sock.close()
                self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.ERROR_BLUETOOTH_DISCONNECTED)

        self.__client_sock.close()
        self.__server_sock.close()
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.ERROR_BLUETOOTH_DISCONNECTED)

    def write(self, data):
        if self.__client_sock is not None:
            self.__client_sock.send(data)

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
        print ":".join(x.encode('hex') for x in self.data)
        (command, payloadSize) = struct.unpack_from('<Bi', self.data)

        if command == COMMAND_ARM:
            None
        elif command == COMMAND_UNARM:
            None
        elif command == COMMAND_START_INSPECTION:
            self.__transferPath()
        elif command == COMMAND_END_INSPECTION:
            None
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
        elif command == COMMAND_READY_TO_TRANSFER:
            print "packing images..."
            self.__packImages()
        elif command == COMMAND_BLUETOOTH_RETURN_HOME:
            self.__return_home()

    def __decipherRcvdPoints(self, payloadSize):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_GET_POINTS, self.__unpackagePoints(payloadSize))

    def __unpackagePoints(self, payloadSize):
        offset = struct.calcsize('<Bi')
        windPos = struct.calcsize('<d')
        numPoints = (payloadSize/windPos)/2

        points = []

        for i in range(0, numPoints):
            (lat, ) = struct.unpack_from('>d', self.data, offset)
            offset += windPos
            print "lat %f" % lat
            (lng, ) = struct.unpack_from('>d', self.data, offset)
            points.append(LocationGlobalRelative(lat,lng))
            offset += windPos

        return points

    def __calculatePath(self, payloadSize):
        newhouse = house(self.__unpackagePoints(payloadSize))
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_NEW_HOUSE, [newhouse])
        return newhouse.path

    def __transferPath(self):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.TRANSFER_PATH)

    def __packImages_rgb(self):
        #packing rgb images
        rgb_img_dir = os.path.join(os.path.expanduser('~'), 'ice-dam-drone', 'images', 'rgb_proc')
        os.chdir(rgb_img_dir)
        with open('images.json', 'rb') as jsonFile:
            f = jsonFile.read()
            json_bytes = bytearray(f)
            json_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_JSON_RGB, json_bytes, self.bluetooth)
            json_packager.run()
        img_list = [str(file) for file in glob.glob("*.jpg")]
        #TODO: find right type
        type = 0
        #img_list = os.listdir(rgb_img_dir)
        for image in img_list:
            img_name_byte = bytearray()
            img_name_byte.extend(image)
            img_path = rgb_img_dir + '/' + image
            img_num = img_list.index(image)
            img_packet = struct.pack('>ii', type, img_num)
            with open(img_path, "rb") as imageFile:
                f = imageFile.read()
                b_img = bytearray(f)
            img_packet.append(b_img)
            img_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_IMAGES_RGB, img_packet, self.bluetooth)
            img_packager.run()

    def __packImages_therm(self):
        #packing thermal images
        therm_img_dir = os.path.join(os.path.expanduser('~'), 'ice-dam-drone', 'images', 'thermal_proc')
        os.chdir(therm_img_dir)
        with open('images.json', 'rb') as jsonFile:
            f = jsonFile.read()
            json_bytes = bytearray(f)
            json_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_JSON_RGB, json_bytes, self.bluetooth)
            json_packager.run()
        img_list = [str(file) for file in glob.glob("*.jpg")]
        #TODO: find right type
        type = 1
        #img_list = os.listdir(therm_img_dir)
        for image in img_list:
            img_name_byte = bytearray()
            img_name_byte.extend(image)
            img_path = therm_img_dir + '/' + image
            img_num = img_list.index(image)
            img_packet = struct.pack('>ii', type, img_num)
            with open(img_path, "rb") as imageFile:
                f = imageFile.read()
                b_img = bytearray(f)
            img_packet.append(b_img)
            img_packager = BlueDataPackager(COMMAND_BLUETOOTH_SEND_IMAGES_THERM, img_packet, self.bluetooth)
            img_packager.run()

    def __return_home(self):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.RETURN_TO_LAUNCH)


class BlueDataPackager(threading.Thread):

    def __init__(self, command, payload, bluetooth):
        super(BlueDataPackager, self).__init__()
        self.command = command
        self.payload = payload
        self.bluetooth = bluetooth

    def run(self):
        if self.command == COMMAND_BLUETOOTH_SEND_PATH:
            self.__sendPath()
        elif self.command == COMMAND_SEND_STATUS:
            self.__sendStatus()
        elif self.command == COMMAND_BLUETOOTH_SEND_IMAGES_RGB or self.command == COMMAND_BLUETOOTH_SEND_IMAGES_THERM:
            self.__sendImage()

    def __sendStatus(self):
        payloadSize = struct.calcsize('>ffdBi')

        data = struct.pack('>BiffdBi', self.command, payloadSize, self.payload[0], self.payload[1], self.payload[2],
                           self.payload[3], self.payload[4])

        self.bluetooth.write(data)

    def __sendPath(self):
        """

        :return:
        """
        numPoints = len(self.payload)
        pointSize = struct.calcsize('>dd')
        payloadSize = numPoints * pointSize

        data = struct.pack('>Bi', self.command, payloadSize)

        for i in range(0, numPoints):
            data = ''.join([data, struct.pack('>dd', self.payload[i].lat, self.payload[i].lon)])

        self.bluetooth.write(data)

    def __sendImage(self):
        payloadSize = len(self.payload)

        data = struct.pack('>Bi', self.command, payloadSize)
        data.append(self.payload)

        self.bluetooth.write(data)