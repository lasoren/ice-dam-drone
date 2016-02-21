from girodicer import Girodicer
from blue import Blue
import EventHandler
import argparse


def bluetoothConnected():
    print "Bluetooth Connected"


def bluetoothDisconnected():
    print "Bluetooth Disconnected"


def getStatus():
    for state in iceCutter.get_status():
        print state


parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyAMA0', help="vehicle connection target")
parser.add_argument('--baud', default='57600', help="connection baud rate")
args = parser.parse_args()

eventQueue = EventHandler.EventQueue()
eventQueue.addEventCallback(bluetoothConnected, EventHandler.BLUETOOTH_CONNECTED)
eventQueue.addEventCallback(bluetoothDisconnected, EventHandler.BLUETOOTH_DISCONNECTED)
eventQueue.addEventCallback(getStatus, EventHandler.GET_STATUS)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = Girodicer(args.connect, args.baud)

blue = Blue(eventQueue)

running = True

while running:
    try:
        eventQueue.execute()
    except KeyboardInterrupt:
        running = False
        blue.stop()
