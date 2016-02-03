from girodicer import Girodicer
from lidar import Lidar
import argparse, threading

parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyAMA0', help="vehicle connection target")
parser.add_argument('--baud', default='57600', help="connection baud rate")
args = parser.parse_args()

print "Connecting to vehicle on: %s" % args.connect
iceCutter = Girodicer(args.connect, args.baud)

lidar = Lidar()

if lidar.isOkay() and lidar.isReady():
    thread = threading.Thread(target=lidar.getDistance())
    thread.start()