import math, sys, utm
import copy
from dronekit import LocationGlobalRelative

def printLatLong(list):
    for item in list:
        print "%.10f, %.10f" % (item.lat, item.lon)

class UTMPoint:

    def __init__(self, UTM):
        self.e = UTM[0]
        self.n = UTM[1]
        self.zone = UTM[2]
        self.zoneLetter = UTM[3]

    def getVector(self, point):
        e = self.e - point.e
        n = self.n - point.n

        diff = self.diff(point)
        mag = math.sqrt( math.pow(diff.e, 2) + math.pow(diff.n,2) )

        return UTMPoint((e/mag, n/mag, self.zone, self.zoneLetter))

    def getVector_nomag(self, point):
        e = self.e - point.e
        n = self.n - point.n

        diff = self.diff(point)
        mag = math.sqrt( math.pow(diff.e, 2) + math.pow(diff.n,2) )

        return UTMPoint((e, n, self.zone, self.zoneLetter))

    def add(self, point):
        return UTMPoint((self.e + point.e, self.n + point.n, self.zone, self.zoneLetter))

    def diff(self, point):
        return UTMPoint((self.e - point.e, self.n - point.n, self.zone, self.zoneLetter))

    def dot(self, point):
        dot = self.e * point.e + self.n * point.n
        if dot < -1:
            return -1.0
        elif dot > 1:
            return 1.0
        return dot

    def dist(self, point):
        e = self.e - point.e
        n = self.n - point.n
        return math.sqrt(math.pow(e, 2) + math.pow(n, 2))

    def toLatLon(self, truncate=5):
        conv = utm.to_latlon(self.e, self.n, self.zone, self.zoneLetter)
        return LocationGlobalRelative(round(conv[0],truncate), round(conv[1],truncate))

    def scalarMult(self, scalar):
        return UTMPoint((self.e * scalar, self.n*scalar, self.zone, self.zoneLetter))

class house:
    def __init__(self, outline):
        self.utmOutline = []
        self.zone = 0
        self.zoneLetter = ""
        self.width = []
        self.height = []

        self.cameraWidth = 8
        self.cameraHeight = 6

        self.path = []
        self.convexHull = []
        self.sbb = []
        self.area = []

        self.centroid = None

        self.houseHeight = 7

        self.outline = outline
        self.__organize_outline_cw()
        print "-------"
        self.__findFlyOverPath()
        for point in self.path:
            print "%f,%f" % (point.lat, point.lon)
        self.__offset_outline()

    def __convertToUTM(self):
        for point in self.outline:
            self.utmOutline.append(UTMPoint(utm.from_latlon(point.lat, point.lon)))
        self.zone = self.utmOutline[0].zone
        self.zoneLetter = self.utmOutline[0].zoneLetter

    def __organize_outline_cw(self):
        mlat = sum(x.lat for x in self.outline) / len(self.outline)
        mlon = sum(x.lon for x in self.outline) / len(self.outline)

        def cw_sort(q):
            return (math.atan2(q.lat - mlat, q.lon - mlon) + 2 * math.pi) % (2 * math.pi)
        self.outline.sort(key=cw_sort, reverse=True)

    def __offset_outline(self):
        """
        calculates for each point on the house outline a meter off set so each "point" won't collide into the house
        The wingspan of the drone is 1 meter and distance from the roof is calculated from the center of the drone
            To be a meter from the roof requires that your points be 1.5m offset
        """
        c_e = 0
        c_n = 0

        for p in self.convexHull:
            c_e += p.e
            c_n += p.n

        c_e /= len(self.convexHull)
        c_n /= len(self.convexHull)

        self.centroid = UTMPoint((c_e, c_n, self.sbb[0].zone, self.sbb[0].zoneLetter))

        for i in range(0, len(self.utmOutline)):
            # we multiply each vector by 1.5 since distance is calculated from center of vehicle
            vec = self.centroid.getVector(self.utmOutline[i]).scalarMult(1.5)
            self.utmOutline[i].n -= vec.n
            self.utmOutline[i].e -= vec.e

        self.outline = []

        for p in self.utmOutline:
            self.outline.append(p.toLatLon())

    def __findMinimumBox(self):
        """
        Function to find the minimum bounding box of the outline of the house provided by the user
         Uses the rotating calipers algorithm as described here: https://geidav.wordpress.com/2014/01/23/computing-oriented-minimum-bounding-boxes-in-2d/
         And based off of the code here: https://github.com/geidav/ombb-rotating-calipers/blob/master/ombb.js
        """
        edgeDirections = []

        for i in range(0, len(self.convexHull)): # finds vector directions between each point on the convex hull
            edgeDirections.append(self.convexHull[i].getVector(self.convexHull[(i+1)%len(self.convexHull)]))

        # find the extreme points in our system
        leftId = None
        rightId = None
        topId = None
        bottomId = None
        minPt = UTMPoint((sys.maxint, sys.maxint, self.zone, self.zoneLetter))
        maxPt = UTMPoint((-sys.maxint+1, -sys.maxint+1, self.zone, self.zoneLetter))

        for i in range(0, len(self.convexHull)):
            point = self.convexHull[i]
            if point.e < minPt.e:
                minPt.e = point.e
                leftId = i

            if point.n < minPt.n:
                minPt.n = point.n
                bottomId = i

            if point.e > maxPt.e:
                maxPt.e = point.e
                rightId = i

            if point.n > maxPt.n:
                maxPt.n = point.n
                topId = i

        # setting up our "calipers" which all point in ccw direction
        leftDir = UTMPoint((0.0, -1.0, self.zone, self.zoneLetter))
        rightDir = UTMPoint((0.0, 1.0, self.zone, self.zoneLetter))
        topDir = UTMPoint((-1.0, 0.0, self.zone, self.zoneLetter))
        bottomDir = UTMPoint((1.0, 0.0, self.zone, self.zoneLetter))

        minArea = sys.maxint

        for i in range(0, len(self.convexHull)):
            # calculate the angles required to turn our "calipers" in each direction
            angles = [math.acos(edgeDirections[leftId].dot(leftDir)),
                      math.acos(edgeDirections[rightId].dot(rightDir)),
                      math.acos(edgeDirections[topId].dot(topDir)),
                      math.acos(edgeDirections[bottomId].dot(bottomDir))]

            # pick the smallest angle required to turn the caliper
            smallestAngleIndex = angles.index(min(angles))

            # rotate the "caliper" to the edge with the smallest angle
            if smallestAngleIndex == 0: # left edge
                leftDir = copy.deepcopy(edgeDirections[leftId])
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                topDir = UTMPoint((leftDir.n, -leftDir.e, self.zone, self.zoneLetter))
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                leftId = (leftId+1)%len(self.convexHull)
            elif smallestAngleIndex == 1: # right edge
                rightDir = copy.deepcopy(edgeDirections[rightId])
                leftDir = UTMPoint((-rightDir.e, -rightDir.n, self.zone, self.zoneLetter))
                topDir = UTMPoint((leftDir.n, -leftDir.e, self.zone, self.zoneLetter))
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                rightId = (rightId+1)%len(self.convexHull)
            elif smallestAngleIndex == 2: # top edge
                topDir = copy.deepcopy(edgeDirections[topId])
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                leftDir = UTMPoint((topDir.n, -topDir.e, self.zone, self.zoneLetter))
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                topId = (topId+1)%len(self.convexHull)
            elif smallestAngleIndex == 3: # bottom edge
                bottomDir = copy.deepcopy(edgeDirections[bottomId])
                topDir = UTMPoint((-bottomDir.e, -bottomDir.n, self.zone, self.zoneLetter))
                leftDir = UTMPoint((bottomDir.n, -bottomDir.e, self.zone, self.zoneLetter))
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                bottomId = (bottomId+1)%len(self.convexHull)

            upperRight = self.__calcLineIntersect(self.convexHull[leftId], leftDir, self.convexHull[topId], topDir)
            bottomRight = self.__calcLineIntersect(self.convexHull[rightId], rightDir, self.convexHull[topId], topDir)
            upperLeft = self.__calcLineIntersect(self.convexHull[bottomId], bottomDir, self.convexHull[leftId], leftDir)
            bottomLeft = self.__calcLineIntersect(self.convexHull[bottomId], bottomDir, self.convexHull[rightId], rightDir)

            width = upperLeft.dist(upperRight)
            height = upperLeft.dist(bottomLeft)

            area = width*height

            if area < minArea and self.__containsExtremes([upperLeft, upperRight, bottomLeft, bottomRight], minPt, maxPt):
                minArea = area
                self.sbb = [upperLeft, upperRight, bottomLeft, bottomRight]
                self.area = area
                self.width = width
                self.height = height
                print area

    def __containsExtremes(self, sbb, minPt, maxPt):
        """
        checks whether the SBB we found is too small by seeing how times we exceed the extremes
        we rather end up with a box that is slightly too large than a box that ends up too small
        """
        count = 0

        # we need count how times we exceed the extremes
        for i in range(0, len(sbb)):
            if sbb[i].n < minPt.n:
                count += 1
            if sbb[i].e < minPt.e:
                count += 1
            if sbb[i].n > maxPt.n:
                count += 1
            if sbb[i].e > maxPt.e:
                count += 1

        # if we only exceed the extremes 2 times or less, the box is probably too small
        if(count <= 2):
            return False
        return True

    def __findConvexHull(self): # uses monotone-chain algorithm
        sortedOutline = sorted(self.utmOutline, key=lambda x: x.e)

        lower = []
        for p in sortedOutline:
            while (len(lower) >= 2) and self.__isPointCCW(lower[-2], lower[-1], p):
                lower.pop()
            lower.append(p)

        upper = []
        for p in reversed(sortedOutline):
            while len(upper) >= 2 and self.__isPointCCW(upper[-2], upper[-1], p):
                upper.pop()
            upper.append(p)

        self.convexHull = lower[:-1] + upper[:-1]

    def __isPointCCW(self, p1, p2, p3):
        val = ((p2.n - p1.n) * (p3.e - p1.e)) - ((p2.e - p1.e) * (p3.n - p1.n))
        return True if (val < 0) else False

    def __calcLineIntersect(self, line1, dir1, line2, dir2):
        dd = (dir1.e*dir2.n) - (dir1.n*dir2.e)
        dx = line2.e - line1.e
        dy = line2.n - line1.n
        t = ((dx * dir2.n) - (dy * dir2.e))/dd

        return UTMPoint(((line1.e+ (t*dir1.e)), (line1.n + (t*dir1.n)), self.zone, self.zoneLetter))

    def __findFlyOverPath(self):
        """
        After finding the prerequisites:
         1) Finds # of columns and rows and mid point of the camera box
         2) Calculates the vector angling of the box in east and north
         3) Find a row of points
         4) Iterate through each item in each column and add a unit east
            a) Every last "rows" number of points in the path array is the previous column
        """
        self.__convertToUTM()
        self.__findConvexHull()
        self.__findMinimumBox()

        columns = int(math.ceil(self.width/self.cameraWidth)) + 1
        rows = int(math.ceil(self.height/self.cameraHeight)) + 1

        e = self.cameraWidth/2.0
        n = self.cameraHeight/2.0

        eVec = self.sbb[0].getVector(self.sbb[1])
        nVec = self.sbb[0].getVector(self.sbb[2])

        for i in range(1, rows + 1):
            addN = nVec.scalarMult(i*n)
            self.path.append(self.sbb[0].diff(addN))

        index = 0
        tempPath = []
        for q in range(0, (columns * rows)):
            print "index %d" % index
            addE = eVec.scalarMult(e)
            rowItem = self.path[len(self.path) - index - 1]
            tempPath.append(rowItem.diff(addE))
            index = index + 1
            if index >= rows:
                self.path = self.path + tempPath
                tempPath = []
                index = 0

        self.path = self.path + tempPath

        temp = []
        for p in self.path:
            temp.append(p.toLatLon())
        self.path = temp

if __name__ == "__main__":
    for i in range(0,10):
        points = [LocationGlobalRelative(38.847020,-94.673213), LocationGlobalRelative(38.847085,-94.673144), LocationGlobalRelative(38.847047,-94.673131)
                  , LocationGlobalRelative(38.847099,-94.673094), LocationGlobalRelative(38.847185,-94.673126), LocationGlobalRelative(38.847142,-94.673275)]

        # points = [LocationGlobalRelative(42.336183,-71.115564), LocationGlobalRelative(42.336076,-71.115397), LocationGlobalRelative(42.336036,-71.115445),
        #            LocationGlobalRelative(42.335990,-71.115502), LocationGlobalRelative(42.336045,-71.115593), LocationGlobalRelative(42.336095,-71.115669)]

        # points = [LocationGlobalRelative(38.847002141057224,-94.67324059456587), LocationGlobalRelative(38.847066376790465,-94.67327814549208), LocationGlobalRelative(38.84714105728545,-94.67330999672413)
        #           , LocationGlobalRelative(38.847167169328145,-94.67325903475285), LocationGlobalRelative(38.84719171463953,-94.67317387461662), LocationGlobalRelative(38.84720790409559,-94.67311218380928),
        #           LocationGlobalRelative(38.84715776899388,-94.6730924025178), LocationGlobalRelative(38.847102150324076,-94.67307161539793), LocationGlobalRelative(38.84707629937822,-94.67310715466738)
        #             , LocationGlobalRelative(38.84702903651357,-94.67310715466738), LocationGlobalRelative(38.84701676383087,-94.67316180467606)]

        # points = [LocationGlobalRelative(38.84703739238143,-94.67322081327438), LocationGlobalRelative(38.8470948389464,-94.67325031757355), LocationGlobalRelative(38.8471460185743,-94.67327579855919)
        #           , LocationGlobalRelative(38.84701833055644,-94.6732097491622), LocationGlobalRelative(38.84716064131836,-94.67323154211044), LocationGlobalRelative(38.84717918086455,-94.67317286878824),
        #           LocationGlobalRelative(38.847189103436605,-94.67313196510077), LocationGlobalRelative(38.847147324176575,-94.67311050742865), LocationGlobalRelative(38.84711024506275,-94.67309340834618)
        #             , LocationGlobalRelative(38.84708961653332,-94.67310950160027), LocationGlobalRelative(38.8470794328267,-94.67314571142197), LocationGlobalRelative(38.84705488747659,-94.67313531786203), LocationGlobalRelative(38.84703269220589,-94.67316649854183)]
        myHouse = None
        convexHull = None
        sbb = None
        path = None
        myHouse = house(points)
        convexHull = []
        sbb = []
        path = []

        for c in myHouse.convexHull:
            convexHull.append(c.toLatLon())

        for b in myHouse.sbb:
            sbb.append(b.toLatLon())

        # for q in myHouse.path:
        #     path.append(q.toLatLon())

        print "original"
        printLatLong(points)

        print "convex"
        printLatLong(convexHull)

        print "sbb"
        printLatLong(sbb)

        print "path"
        printLatLong(myHouse.path)

        print myHouse.area

        printLatLong(myHouse.outline)
