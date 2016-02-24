import Queue, threading

BLUETOOTH_CONNECTED = 1
BLUETOOTH_DISCONNECTED = -1
GET_STATUS = 2
BLUETOOTH_GET_POINTS = 3
LIDAR_DISTANCE = 4
BLUETOOTH_SEND_PATH = 5

DEFAULT_PRIORITY = 5

class EventQueue:
    def __init__(self):
        self.__queue = Queue.PriorityQueue()
        self.__callbacks = {}
        self.__condition = threading.Condition()

    def add(self, priority, event, args=[], kwargs={}):
        self.__queue.put((priority, (event, args, kwargs)))

    def execute(self):
        (event, args, kwargs) = self.__queue.get()
        with self.__condition:
            func = self.__callbacks.get(event)
            self.__condition.notify()
        func(*args, **kwargs)

    def addEventCallback(self, func, event):
        with self.__condition:
            self.__callbacks[event] = func
            self.__condition.notify()
