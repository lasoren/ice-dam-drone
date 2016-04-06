import Queue, threading

BLUETOOTH_CONNECTED = 1
BLUETOOTH_GET_POINTS = 3
LIDAR_DISTANCE = 4
BLUETOOTH_NEW_HOUSE = 5
BLUETOOTH_TRANSFER_DATA = 6
BLUETOOTH_SEND_STATUS = 2
SCAN_BORDER_FINISHED = 7
SCAN_ROOF_FINISHED = 8

DEFAULT_PRIORITY = 5
ERROR_PRIORITY = 4
HIGH_PRIORITY = 1

ERROR_BLUETOOTH_DISCONNECTED = -1
ERROR_BORDER_SCAN_INTERRUPTED = -2
ERROR_ROOF_SCAN_INTERRUPTED = -3

EXIT_QUEUE = 0

class EventQueue:
    def __init__(self):
        self.__queue = Queue.PriorityQueue()
        self.__callbacks = {}
        self.__condition = threading.Condition()

    def add(self, priority, event, args=[], kwargs={}):
        self.__queue.put((priority, (event, args, kwargs)))

    def execute(self):
        (priority,(event, args, kwargs)) = self.__queue.get()
        with self.__condition:
            func = self.__callbacks.get(event)
            self.__condition.notify()
        func(*args, **kwargs)

    def addEventCallback(self, func, event):
        with self.__condition:
            self.__callbacks[event] = func
            self.__condition.notify()
