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
                    data = self.__client_sock.recv()
                    BlueDataProcessor(data, self.queue)
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
    COMMAND_CALCULATE_PATH = 0x8
    COMMAND_BLUETOOTH_SEND_PATH = 0x9

    def __init__(self, data, queue):
        super(BlueDataProcessor, self).__init__()
        self.data = data
        self.queue = queue
        self.start()
        self.dataPacker = BlueDataPackager()

    def run(self):
        (command, payloadSize) = struct.unpack('Bi', self.data)

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
        elif command == self.COMMAND_CALCULATE_PATH:
            path = self.__calculatepath(points)
            #newDataPacker.run(COMMAND_BLUETOOTH_SEND_PATH,__packagePoints(path))
            package = self.__packagePoints(path)
            data = bytearray()
            data.append(self.COMMAND_BLUETOOTH_SEND_PATH)
            data.append(chr(sys.getsizeof(package)))
            data.append(package)
            self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_TRANSFER_DATA, data)
        elif command == self.COMMAND_READY_TO_TRANSFER:
            None

    def __decipherRcvdPoints(self, payloadSize):
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_GET_POINTS, self.__unpackagePoints(payloadSize))

    def __unpackagePoints(self, payloadSize):
        offset = struct.calcsize('Bi')
        windPos = struct.calcsize('dd')
        numPoints = payloadSize/windPos

        points = []

        for i in range(0, numPoints):
            (lat, lng) = struct.unpack('dd', offset)
            points.append(geoPoint(lat,lng))
            offset += windPos

        return points

    def __packagePoints(self, points):
        package = bytearray()

        for pair in points:
            package += struct.pack('f', pair.lon)
            package += struct.pack('f', pair.lat)

        return package

    def __calculatePath(self, payloadSize):
        newhouse = house(self.__unpackagePoints(payloadSize))
        self.queue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.BLUETOOTH_SEND_PATH, newhouse)
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

    def __init__(self, command, payload):
        super(BlueDataPackager, self).__init__()
        self.command = command
        self.payload = payload

    def run(self):
        blank

