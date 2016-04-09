import girodicer
import EventHandler, argparse, os


def bluetoothConnected():
    print "Bluetooth Connected"
    iceCutter.get_status()

def bluetoothDisconnected():
    print "Bluetooth Disconnected"
    iceCutter.return_to_launch()

def printPoints(points):
    for point in points:
        print "lat: %d, lng: %d" % point[0] % point[1]

def setHouse(house):
    iceCutter.house = house

def transferPath():
    #add functionality here
    print "Transferring path to Girodicer..."

def handleBorderInterrupt():
    # TODO: implement handle border interruption
    None

def handleRoofInterrupt():
    # TODO: implement handle roof interruption
    None

def handleRoofFinished():
    # TODO: implement handle roof finished
    # start analysis of images, land, and send pictures
    iceCutter.return_to_launch()

def bluetoothSendStatus():
    iceCutter.get_status()

def return_home():
    iceCutter.return_to_launch()


parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyAMA0', help="vehicle connection target")
parser.add_argument('--baud', default='57600', help="connection baud rate")
parser.add_argument('--debug', default=False, help="enable debug option")
args = parser.parse_args()

eventQueue = EventHandler.EventQueue()
eventQueue.addEventCallback(bluetoothConnected, EventHandler.BLUETOOTH_CONNECTED)
eventQueue.addEventCallback(bluetoothDisconnected, EventHandler.ERROR_BLUETOOTH_DISCONNECTED)
eventQueue.addEventCallback(printPoints, EventHandler.BLUETOOTH_GET_POINTS)
eventQueue.addEventCallback(setHouse, EventHandler.BLUETOOTH_NEW_HOUSE)
eventQueue.addEventCallback(handleRoofFinished, EventHandler.SCAN_ROOF_FINISHED)
eventQueue.addEventCallback(handleBorderInterrupt, EventHandler. ERROR_BORDER_SCAN_INTERRUPTED)
eventQueue.addEventCallback(handleRoofInterrupt, EventHandler.ERROR_ROOF_SCAN_INTERRUPTED)
eventQueue.addEventCallback(bluetoothSendStatus, EventHandler.BLUETOOTH_SEND_STATUS)
eventQueue.addEventCallback(return_home, EventHandler.RETURN_TO_LAUNCH)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = girodicer.Girodicer(args.connect, args.baud, eventQueue, args.debug)

running = True

while running:
    try:
        eventQueue.execute()
    except KeyboardInterrupt:
        running = False
        iceCutter.stop()
