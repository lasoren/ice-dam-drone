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

def printLidarDistance(distance):
    print "Distance %d" % distance

parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyAMA0', help="vehicle connection target")
parser.add_argument('--baud', default='57600', help="connection baud rate")
args = parser.parse_args()

eventQueue = EventHandler.EventQueue()
eventQueue.addEventCallback(bluetoothConnected, EventHandler.BLUETOOTH_CONNECTED)
eventQueue.addEventCallback(bluetoothDisconnected, EventHandler.BLUETOOTH_DISCONNECTED)
eventQueue.addEventCallback(printPoints, EventHandler.BLUETOOTH_GET_POINTS)
eventQueue.addEventCallback(printLidarDistance, EventHandler.LIDAR_DISTANCE)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = girodicer.Girodicer(args.connect, args.baud, eventQueue)

running = True

while running:
    try:
        eventQueue.execute()
    except KeyboardInterrupt:
        running = False
        iceCutter.stop()
