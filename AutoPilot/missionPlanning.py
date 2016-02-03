from girodicer import Girodicer
from lidar import Lidar
import argparse, signal

loop = True

def sigIntHandler(signum ,frame):
    global loop
    loop = False

parser = argparse.ArgumentParser(description="Start the AutoMission Planner. Default connects to ArduPilot over Serial")
parser.add_argument('--connect', default='/dev/ttyAMA0', help="vehicle connection target")
parser.add_argument('--baud', default='57600', help="connection baud rate")
args = parser.parse_args()

signal.signal(signal.SIGINT, sigIntHandler)

print "Connecting to vehicle on: %s" % args.connect
iceCutter = Girodicer(args.connect, args.baud)

lidar = Lidar()
lidar.start()

while loop:
    if lidar.readDistance() < 10:
        iceCutter.setVelocity(-5, 0, 0, 5)

lidar.stop()
lidar.join()