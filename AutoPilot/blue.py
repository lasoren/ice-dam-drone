from bluetooth import *
import EventHandler
import threading

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
        self.__running = True
        self.start()

    def run(self):
        while self.__running:
            self.__client_sock, client_info = self.__server_sock.accept()
            self.queue.add(EventHandler.BLUETOOTH_CONNECTED)
            try:
                while True:
                    data = self.__client_sock.recv(1024)
                    if len(data) == 0: break
                    print "received [%s]" % data
                    if data == 'transfer':
                        self.queue.add(EventHandler.GET_STATUS)
            except IOError:
                self.queue.add(EventHandler.BLUETOOTH_DISCONNECTED)

        self.__client_sock.close()
        self.__server_sock.close()
        self.queue.add(EventHandler.BLUETOOTH_DISCONNECTED)

    def write(self, data):
        self.__client_sock.send(data)

    def stop(self):
        self.__running = False
