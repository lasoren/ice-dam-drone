import girodicer
import EventHandler, argparse, os


def bluetoothConnected():
    print "Bluetooth Connected"
    iceCutter.get_status()


def bluetoothDisconnected():
    print "Bluetooth Disconnected"


def printPoints(points):
    for point in points:
        print "lat: %d, lng: %d" % point[0] % point[1]

def setHouse(house):
    iceCutter.house = house

def transferPath():
    #add functionality here
    print "Transferring path to Girodicer..."

def transferData(data): # dunno if I'm gonna use yet...
    #generic transfer of data from drone to Android App
    if iceCutter.blue.__stop().isSet() is True:
        print "Error: cannot send data, bluetooth service stopped"
    else:
        iceCutter.blue.write(data)



parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyAMA0', help="vehicle connection target")
parser.add_argument('--baud', default='57600', help="connection baud rate")
parser.add_argument('--debug', default=False, help="enable debug option")
args = parser.parse_args()

eventQueue = EventHandler.EventQueue()
eventQueue.addEventCallback(bluetoothConnected, EventHandler.BLUETOOTH_CONNECTED)
eventQueue.addEventCallback(bluetoothDisconnected, EventHandler.BLUETOOTH_DISCONNECTED)
eventQueue.addEventCallback(printPoints, EventHandler.BLUETOOTH_GET_POINTS)
eventQueue.addEventCallback(setHouse, EventHandler.BLUETOOTH_NEW_HOUSE)
eventQueue.addEventCallback(transferData, EventHandler.BLUETOOTH_TRANSFER_DATA)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = girodicer.Girodicer(args.connect, args.baud, eventQueue)

running = True

while running:
    try:
        eventQueue.execute()
    except KeyboardInterrupt:
        running = False
        iceCutter.stop()
