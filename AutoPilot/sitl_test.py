import girodicer, EventHandler, argparse, threading, house
from dronekit import LocationGlobalRelative, VehicleMode

##### Test Functions #####

def load_new_house():
    points = [LocationGlobalRelative(38.847195892564024,-94.67311520129442), LocationGlobalRelative(38.847113900750884,-94.67307429760695), LocationGlobalRelative(38.84709144437794,-94.67313230037689)
              , LocationGlobalRelative(38.847141579526394,-94.67326674610376), LocationGlobalRelative(38.84702564194198,-94.67319834977388), LocationGlobalRelative(38.84705097066462,-94.67311218380928)]

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
    while running:
        eventQueue.execute()


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
eventQueue.addEventCallback(handleRoofFinished, EventHandler.SCAN_ROOF_FINISHED)
eventQueue.addEventCallback(handleBorderInterrupt, EventHandler. ERROR_BORDER_SCAN_INTERRUPTED)
eventQueue.addEventCallback(handleRoofInterrupt, EventHandler.ERROR_ROOF_SCAN_INTERRUPTED)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = girodicer.Girodicer(args.connect, args.baud, eventQueue)

running = True
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
        else:
            print "try again"
    except KeyboardInterrupt:
        break

running = False
run_loop.join()
iceCutter.stop()
print "Exiting"
