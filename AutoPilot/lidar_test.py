from lidar import Lidar
from datetime import datetime

sensor = Lidar()

start = datetime.now()
count = 0
totalTime = 0
longest = 0
longestAt = 0
while (datetime.now() - start).seconds < 10:
    count += 1
    acquire = datetime.now()
    sensor.acquireLock()
    print "Distance %d\n" % sensor.distance
    sensor.releaseLock()
    release = datetime.now()
    delay = (release - acquire).microseconds
    totalTime += delay

    if delay > longest:
        longest = delay
        longestAt = count

sensor.stop()
sensor.join()
print "Killed Lidar Thread"

print "Average time %f\n" % (totalTime / count)
print "Longest delay %f\n" % longest
print "Longest delay occured at loop # %d\n" % longestAt