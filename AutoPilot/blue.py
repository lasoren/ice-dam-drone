from bluetooth import *
import EventHandler
import threading
from house import *


class Blue(threading.Thread):
    __uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    __name = "ubuntu-mate-0"
    __client_sock = None

    def __init__(self, queue):
        """
        Initializes the bluetooth system and will start itself as a thread
        :param queue: Main Event Queue
        """
        super(Blue, self).__init__()
        self.__server_sock = BluetoothSocket(RFCOMM)
        self.__server_sock.bind(("", PORT_ANY))
        self.__server_sock.listen(1)

        advertise_service(self.__server_sock, self.__name,
                          service_id=self.__uuid,
                          service_classes=[self.__uuid, SERIAL_PORT_CLASS],
                          profiles=[SERIAL_PORT_PROFILE])

        self.queue = queue
        self.__stop = threading.Event()
        self.start()

    def run(self):
        self.__stop.clear()
        while self.__stop.isSet() is False:
            self.__client_sock, client_info = self.__server_sock.accept()
            self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_CONNECTED)
            try:
                while True:
                    data = self.__client_sock.recv(1024)
                    BlueDataProcessor(data, self.queue, self)
            except IOError:
                self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_DISCONNECTED)

        self.__client_sock.close()
        self.__server_sock.close()
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_DISCONNECTED)

    def write(self, data):
        self.__client_sock.send(data)

    def stop(self):
        self.__stop.set()


class BlueDataProcessor(threading.Thread):
    COMMAND_ARM = 0x1
    COMMAND_UNARM = 0x2
    COMMAND_START_INSPECTION = 0x3
    COMMAND_END_INSPECTION = 0x4
    COMMAND_STATUS = 0x5
    COMMAND_SEND_POINTS = 0x6
    COMMAND_READY_TO_TRANSFER = 0x7
    COMMAND_NEW_HOUSE = 0x8
    COMMAND_BLUETOOTH_SEND_PATH = 0x9

    def __init__(self, data, queue, bluetooth):
        super(BlueDataProcessor, self).__init__()
        self.data = data
        self.queue = queue
        self.bluetooth = bluetooth
        self.start()

    def run(self):
        print ":".join(x.encode('hex') for x in self.data)
        (command, payloadSize) = struct.unpack_from('<Bi', self.data)

        if command == self.COMMAND_ARM:
            None
        elif command == self.COMMAND_UNARM:
            None
        elif command == self.COMMAND_START_INSPECTION:
            self.__transferPath()
        elif command == self.COMMAND_END_INSPECTION:
            None
        elif command == self.COMMAND_STATUS:
            None
        elif command == self.COMMAND_SEND_POINTS:
            self.__decipherRcvdPoints(payloadSize)
        elif command == self.COMMAND_NEW_HOUSE:
            print "new house"
            print "payloadsize %d" % payloadSize
            path = self.__calculatePath(payloadSize)
            packager = BlueDataPackager(self.COMMAND_BLUETOOTH_SEND_PATH, path, self.bluetooth)
            packager.run()
        elif command == self.COMMAND_READY_TO_TRANSFER:
            None

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
            points.append(geoPoint(lat,lng))
            offset += windPos

        return points

    def __calculatePath(self, payloadSize):
        newhouse = house(self.__unpackagePoints(payloadSize))
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_NEW_HOUSE, [newhouse])
        return newhouse.path

    def __transferPath(self):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.TRANSFER_PATH)


class BlueDataPackager(threading.Thread):
    COMMAND_ARM = 0x1
    COMMAND_UNARM = 0x2
    COMMAND_START_INSPECTION = 0x3
    COMMAND_END_INSPECTION = 0x4
    COMMAND_STATUS = 0x5
    COMMAND_SEND_POINTS = 0x6
    COMMAND_READY_TO_TRANSFER = 0x7
    COMMAND_NEW_HOUSE = 0x8
    COMMAND_BLUETOOTH_SEND_PATH = 0x9

    def __init__(self, command, payload, bluetooth):
        super(BlueDataPackager, self).__init__()
        self.command = command
        self.payload = payload
        self.bluetooth = bluetooth

    def run(self):
        if self.command == self.COMMAND_BLUETOOTH_SEND_PATH:
            self.__sendPath()
        elif self.command == self.COMMAND_STATUS:
            self.__sendStatus()

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
