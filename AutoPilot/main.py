import girodicer
import EventHandler, argparse, os, time
from dronekit import APIException


def bluetoothConnected():
    print "Bluetooth Connected"
    iceCutter.get_status()

def bluetoothDisconnected():
    print "Bluetooth Disconnected"
    iceCutter.status.stop()
    iceCutter.return_to_launch()

def printPoints(points):
    iceCutter.set_ice_dams(points)
    for point in points:
        print "lat: %d, lng: %d" % (point[0], point[1])

def setHouse(house):
    iceCutter.house = house

def handleBorderInterrupt():
    iceCutter.return_to_launch()

def handleRoofInterrupt():
    iceCutter.return_to_launch()

def handleRoofFinished():
    startAnalysis()
    iceCutter.return_to_launch()

def bluetoothSendStatus():
    iceCutter.get_status()

def startInspection():
    iceCutter.start_scan()

def startAnalysis():
    iceCutter.process_images()

def return_home():
    iceCutter.return_to_launch()

def battery_low():
    return_home()

def service_icedam():
    iceCutter.start_service_ice_dams()


parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyS0', help="vehicle connection target")
parser.add_argument('--baud', default='115200', help="connection baud rate")
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
eventQueue.addEventCallback(startAnalysis, EventHandler.START_ANALYSIS)
eventQueue.addEventCallback(startInspection, EventHandler.START_SCAN)
eventQueue.addEventCallback(service_icedam, EventHandler.SERVICE_ICE_DAM)

print "Connecting to vehicle on: %s" % args.connect
while True:
    try:
        iceCutter = girodicer.Girodicer(args.connect, args.baud, eventQueue, args.debug)
        break
    except APIException:
        print "Failed to connect. Retrying in 5 seconds..."
        time.sleep(5)
        pass
running = True

while running:
    try:
        eventQueue.execute()
    except KeyboardInterrupt:
        running = False
        iceCutter.stop()
