import girodicer, EventHandler, argparse, threading, house
from dronekit import LocationGlobalRelative, VehicleMode

##### Test Functions #####

def load_new_house():
    points = [LocationGlobalRelative(38.84719,-94.67311), LocationGlobalRelative(38.84711,-94.67307), LocationGlobalRelative(38.84709,-94.67313)
              , LocationGlobalRelative(38.84714,-94.67326), LocationGlobalRelative(38.84702,-94.67319), LocationGlobalRelative(38.84705,-94.67311)]

    iceCutter.house = house.house(points)

def start_flight():
    iceCutter.start_scan()

def emergency_stop():
    iceCutter.vehicle.mode = VehicleMode("LOITER")

##### Event Handlers ######
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

def transferData(data): # dunno if I'm gonna use yet...
    #generic transfer of data from drone to Android App
    if iceCutter.blue.__stop().isSet() is True:
        print "Error: cannot send data, bluetooth service stopped"
    else:
        iceCutter.blue.write(data)

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

def test_loop(iceCutter, eventQueue, running):
    while running.isSet() is False:
        eventQueue.execute()

def leave_queue():
    print "High priority quit"


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
eventQueue.addEventCallback(transferData, EventHandler.BLUETOOTH_TRANSFER_DATA)
eventQueue.addEventCallback(handleRoofFinished, EventHandler.SCAN_ROOF_FINISHED)
eventQueue.addEventCallback(handleBorderInterrupt, EventHandler. ERROR_BORDER_SCAN_INTERRUPTED)
eventQueue.addEventCallback(handleRoofInterrupt, EventHandler.ERROR_ROOF_SCAN_INTERRUPTED)
eventQueue.addEventCallback(leave_queue, EventHandler.EXIT_QUEUE)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = girodicer.Girodicer(args.connect, args.baud, eventQueue, args.debug)

running = threading.Event()
running.clear()
run_loop = threading.Thread(target=test_loop, args=(iceCutter, eventQueue, running))
run_loop.start()

while True:
    try:
        command = raw_input("Command:")

        if command == "l":
            load_new_house()
        elif command == "g":
            start_flight()
        elif command == "e":
            emergency_stop()
            break
        else:
            print "try again"
    except KeyboardInterrupt:
        break

running.set()
eventQueue.add(EventHandler.HIGH_PRIORITY, EventHandler.EXIT_QUEUE)
run_loop.join()
iceCutter.stop()
print "Exiting"
