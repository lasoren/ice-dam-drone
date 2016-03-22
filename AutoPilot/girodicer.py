from dronekit import connect, VehicleMode, LocationGlobal, LocationGlobalRelative, mavutil
import blue, EventHandler
import time, math, threading, lidar, datetime, os

class Girodicer():

    status = None
    house = None
    folders = []

    flying_velocity = 1.4 # m/s

    def __init__(self, connection, baud, queue, debug=False):
        self.debug = debug
        self.eventQueue = queue
        self.eventQueue.addEventCallback(self.__roof_scan,EventHandler.SCAN_BORDER_FINISHED)
        print "Initializing vehicle"
        self.vehicle = connect(connection, baud=baud, wait_ready=True)
        self.vehicle.airspeed = 1.4
        print "Initializing lidar"
        if not debug:
            self.lidar = lidar.Lidar()
        else:
            self.lidar = None
        print "Initializing bluetooth"
        self.blue = blue.Blue(self.eventQueue, debug)
        # print "Initializing Lidar"
        # self.lidar = lidar.Lidar()
        # self.lidar.start()

    def arm_vehicle(self):
        """
        Arms vehicle
        """
        while not self.vehicle.is_armable:
            # we might want to initiate a bluetooth connection so the user can do pre-arm checks on their phone
            print "Waiting for vehicle to initialise"
            time.sleep(1)

        print "Vehicle Armed!!!"
        self.vehicle.mode = VehicleMode("LOITER")
        self.vehicle.armed = True

    def disarm_vehicle(self):
        self.vehicle.mode = VehicleMode("LOITER")
        self.vehicle.armed = False

    def cancel_movement(self):
        self.vehicle.mode = VehicleMode("LOITER")

    def return_to_launch(self):
        self.vehicle.mode = VehicleMode("RETURN_TO_LAUNCH")

    def setVelocity(self, velocityX, velocityY, velocityZ, duration):
        """
        Sets the velocity of the drone in the NED coordinate system
        Message needs to be sent at 1Hz
        """

        if not(self.vehicle.armed):
            # don't do anything since the motors aren't armed
            return

        msg = self.vehicle.message_factory.set_position_target_local_ned_encode(
        0,       # time_boot_ms (not used)
        0, 0,    # target system, target component
        mavutil.mavlink.MAV_FRAME_LOCAL_NED, # frame
        0b0000111111000111, # type_mask (only speeds enabled)
        0, 0, 0, # x, y, z positions (not used)
        velocityX, velocityY, velocityZ, # x, y, z velocity in m/s
        0, 0, 0, # x, y, z acceleration (not supported yet, ignored in GCS_Mavlink)
        0, 0)    # yaw, yaw_rate (not supported yet, ignored in GCS_Mavlink)

        # send command to vehicle on 1 Hz cycle
        for x in range(0,duration):
            self.vehicle.send_mavlink(msg)
            time.sleep(1)


    def goto_position(self, north, east, down):
        """
        Send SET_POSITION_TARGET_LOCAL_NED command to request the vehicle fly to a specified
        location in the North, East, Down frame.

        It is important to remember that in this frame, positive altitudes are entered as negative
        "Down" values. So if down is "10", this will be 10 metres below the home altitude.

        Starting from AC3.3 the method respects the frame setting. Prior to that the frame was
        ignored. For more information see:
        http://dev.ardupilot.com/wiki/copter-commands-in-guided-mode/#set_position_target_local_ned

        See the above link for information on the type_mask (0=enable, 1=ignore).
        At time of writing, acceleration and yaw bits are ignored.

        """
        msg = self.vehicle.message_factory.set_position_target_local_ned_encode(
            0,       # time_boot_ms (not used)
            0, 0,    # target system, target component
            mavutil.mavlink.MAV_FRAME_LOCAL_NED, # frame
            0b0000111111111000, # type_mask (only positions enabled)
            north, east, down, # x, y, z positions (or North, East, Down in the MAV_FRAME_BODY_NED frame
            0, 0, 0, # x, y, z velocity in m/s  (not used)
            0, 0, 0, # x, y, z acceleration (not supported yet, ignored in GCS_Mavlink)
            0, 0)    # yaw, yaw_rate (not supported yet, ignored in GCS_Mavlink)
        # send command to vehicle
        self.vehicle.send_mavlink(msg)

    def get_status(self):
        print "Starting status thread"
        self.status = GirodicerStatus(self.vehicle, self.blue)

    def read_lidar(self):
        return self.lidar.readDistance()

    def start_scan(self):
        if not self.vehicle.armed:
            self.arm_vehicle()

        self.vehicle.mode = VehicleMode("LOITER")

        print "Starting scanning thread"
        scan_t = threading.Thread(target=self.__border_scan)
        scan_t.start()

    def stop(self):
        self.blue.stop()
        if self.status is not None:
            self.status.stop()
            self.status.join()

        self.blue.join()

    def __border_scan(self):
        """
        function to fly the border perimeter of the house
        initially flies to the first point of the house and waits until its been reached

        after finishing it will fire an event to the main thread signalling that it has finished the border
        """
        print "Starting border scan"
        start_point = self.house.outline[0]
        start_point.alt = self.house.houseHeight

        fly_to_start = threading.Thread(target=self.__fly_single_point, args=(start_point,))
        fly_to_start.start()

        #  while drone is flying to start point, set up camera
        camera = GirodicerCamera(self.vehicle)
        # camera.start()

        fly_to_start.join()

        for i in range(1, len(self.house.outline)):
            point = self.house.outline[i]
            point.alt = self.house.houseHeight
            point_distance = self.__get_distance_metres(self.vehicle.location.global_frame, point)

            while self.vehicle.mode == "GUIDED":
                distance = self.__get_distance_metres(self.vehicle.location.global_frame, point)
                if distance <= (point_distance * 0.01):
                    break
                time.sleep(0.5)

        # stop camera and save folder location
        print "Finished Border Scan"

        self.folders.append(camera.stop())

        if self.vehicle.mode == "GUIDED":
            self.eventQueue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.SCAN_BORDER_FINISHED)
        else:
            self.eventQueue.add(EventHandler.ERROR_PRIORITY, EventHandler.ERROR_BORDER_SCAN_INTERRUPTED)

    def __roof_scan(self):
        """
        function to fly over the roof
        initially flies to the first point of the roof and waits until its been reached

        should be called after border scan has finished
        """

        print "Roof scan"
        high_point = self.vehicle.location.global_frame
        high_point.alt *= 3

        # need to drone to be way above the house so we don't collide with anything
        # this  will have the drone fly high above its current point
        rise = threading.Thread(target=self.__fly_single_point, args=high_point)
        rise.start()
        rise.join()

        start_point = self.house.path[0]
        start_point.alt = self.house.houseHeight*2

        fly_to_start = threading.Thread(target=self.__fly_single_point, args=(start_point,))
        fly_to_start.start()

        # TODO: Implement thermal camera start up here

        fly_to_start.join()

        for i in range(1, len(self.house.path)):
            point = self.house.path[i]
            point.alt = self.house.houseHeight*2
            point_distance = self.__get_distance_metres(self.vehicle.location.global_frame, point)

            while self.vehicle.mode == "GUIDED":
                distance = self.__get_distance_metres(self.vehicle.location.global_frame, point)
                if distance <= (point_distance * 0.01):
                    break
                time.sleep(0.5)

        print "Finished roof scan"

        if self.vehicle.mode == "GUIDED":
            self.eventQueue.add(EventHandler.DEFAULT_PRIORITY, EventHandler.SCAN_ROOF_FINISHED)
        else:
            self.eventQueue.add(EventHandler.ERROR_PRIORITY, EventHandler.ERROR_ROOF_SCAN_INTERRUPTED)


    def __fly_single_point(self, destination):
        dist_destination = self.__get_distance_metres(self.vehicle.location.global_frame, destination)

        self.vehicle.simple_goto(destination)

        while self.vehicle.mode == "GUIDED":
            distance = self.__get_distance_metres(self.vehicle.location.global_frame, destination)
            if distance <= (dist_destination * 0.01):
                break
            time.sleep(0.5)

    def __get_location_metres(self, original_location, dNorth, dEast):
        """
        Returns a LocationGlobal object containing the latitude/longitude `dNorth` and `dEast` metres from the
        specified `original_location`. The returned LocationGlobal has the same `alt` value
        as `original_location`.

        The function is useful when you want to move the vehicle around specifying locations relative to
        the current vehicle position.

        The algorithm is relatively accurate over small distances (10m within 1km) except close to the poles.

        For more information see:
        http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
        """
        earth_radius=6378137.0 #Radius of "spherical" earth
        #Coordinate offsets in radians
        dLat = dNorth/earth_radius
        dLon = dEast/(earth_radius*math.cos(math.pi*original_location.lat/180))

        #New position in decimal degrees
        newlat = original_location.lat + (dLat * 180/math.pi)
        newlon = original_location.lon + (dLon * 180/math.pi)
        if type(original_location) is LocationGlobal:
            targetlocation=LocationGlobal(newlat, newlon,original_location.alt)
        elif type(original_location) is LocationGlobalRelative:
            targetlocation=LocationGlobalRelative(newlat, newlon,original_location.alt)
        else:
            raise Exception("Invalid Location object passed")

        return targetlocation

    def __get_distance_metres(self, aLocation1, aLocation2):
        """
        Returns the ground distance in metres between two LocationGlobal objects.

        This method is an approximation, and will not be accurate over large distances and close to the
        earth's poles. It comes from the ArduPilot test code:
        https://github.com/diydrones/ardupilot/blob/master/Tools/autotest/common.py
        """
        dlat = aLocation2.lat - aLocation1.lat
        dlong = aLocation2.lon - aLocation1.lon
        return math.sqrt((dlat*dlat) + (dlong*dlong)) * 1.113195e5

    def __get_bearing(self, aLocation1, aLocation2):
        """
        Returns the bearing between the two LocationGlobal objects passed as parameters.

        This method is an approximation, and may not be accurate over large distances and close to the
        earth's poles. It comes from the ArduPilot test code:
        https://github.com/diydrones/ardupilot/blob/master/Tools/autotest/common.py
        """
        off_x = aLocation2.lon - aLocation1.lon
        off_y = aLocation2.lat - aLocation1.lat
        bearing = 90.00 + math.atan2(-off_y, off_x) * 57.2957795
        if bearing < 0:
            bearing += 360.00
        return bearing

class GirodicerStatus(threading.Thread):
    """
    A threaded class that sends a status update every 0.5 seconds
    """

    def __init__(self, vehicle, bluetooth):
        super(GirodicerStatus, self).__init__()
        self.vehicle = vehicle
        self.bluetooth = bluetooth
        self.__stopped = threading.Event()
        self.start()

    def run(self):
        self.__stopped.clear()
        while self.__stopped.isSet() is False:
            time.sleep(0.5) # only send a message every 0.5 seconds about your state
            location = self.vehicle.location.global_frame
            velocity = self.vehicle.velocity
            state = self.vehicle.system_status
            armable = self.vehicle.is_armable

            payload = (float(38.847004), float(-94.67325), float(velocity[0]), self.__decipherState(state), 1)

            packager = blue.BlueDataPackager(blue.COMMAND_STATUS, payload, self.bluetooth)
            packager.run()

    def stop(self):
        self.__stopped.set()

    def __decipherState(self, state):
        if "UNINIT" == state:
            return 0x0
        elif "BOOT" == state:
            return 0x1
        elif "CALIBRATING" == state:
            return 0x2
        elif "STANDBY" == state:
            return 0x3
        elif "ACTIVE" == state:
            return 0x4
        elif "CRITICAL" == state:
            return 0x5
        elif "EMERGENCY" == state:
            return 0x6
        elif "POWEROFF" == state:
            return 0x7

class GirodicerCamera(threading.Thread):
    """
    A threaded class that takes pictures every 0.1 seconds

    On stop this class will return the folder where pictures were saved to
    """

    def __init__(self, vehicle):
        super(GirodicerCamera, self).__init__()
        self.vehicle = vehicle
        self.__stopped = threading.Event()
        self.folder = str(datetime.datetime.now())
        os.makedirs(self.folder)

    def run(self):
        self.__stopped.clear()
        os.chdir(self.folder)
        while self.__stopped.isSet() is False:
            time.sleep(0.1)
            location = self.vehicle.location.global_frame
            file_name = str(location.lat) + "," + str(location.lon) + ".jpg"
            os.system("fswebcam -r 640x480 --jpeg 85" + file_name)

    def stop(self):
        self.__stopped.set()
        return self.folder