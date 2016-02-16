# test BLE Scanning software
# jcs 6/8/2014

import blescan
import sys
import math

import bluetooth._bluetooth as bluez

dev_id = 0

#defining ranges for the distance
close = 0.4
near = 2

#defining UUID for beacon
estimote = '8de22a544b224e7c9caa96a391f1266a' 

try:
	sock = bluez.hci_open_dev(dev_id)
	print "ble thread started"

except:
	print "error accessing bluetooth device..."
    	sys.exit(1)

blescan.hci_le_set_scan_parameters(sock)
blescan.hci_enable_le_scan(sock)

while True:
	returnedList = blescan.parse_events(sock, 1)
	#print "----------"
	for beacon in returnedList:
		beacon_array = beacon.split(',')
		UUID = beacon_array[1]
		if (UUID==estimote):
			tx = float(beacon_array[4])
			rssi = float(beacon_array[5])
			ratio = rssi/tx
			distance = (0.89976)*math.pow(ratio,7.7095) + 0.111
			print 'UUID:%s Major:%s Minor:%s Tx:%s RSSI:%s' % (beacon_array[1], beacon_array[2], beacon_array[3], beacon_array[4], beacon_array[5])
			print 'Distance: %.2f' % distance
			if (distance <= close):
				print 'Beacon is close to Pi!!!'
			elif(distance>close and distance<near):
				print 'Beacon is near the Pi...'
			else:
				print 'Beacon is far from Pi'
