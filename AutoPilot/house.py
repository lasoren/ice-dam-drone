import math, sys, utm

def printLatLong(list):
    for item in list:
        print "%.10f, %.10f" % (item.lat, item.lon)

class geoPoint:
    lon = 0
    lat = 0

    def __init__(self, lat, lon):
        self.lon = lon
        self.lat = lat

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

    def toLatLon(self):
        conv = utm.to_latlon(self.e, self.n, self.zone, self.zoneLetter)
        return geoPoint(conv[0], conv[1])

    def scalarMult(self, scalar):
        return UTMPoint((self.e * scalar, self.n*scalar, self.zone, self.zoneLetter))

class house:
    utmOutline = []
    zone = 0
    zoneLetter = ""
    width = 0
    height = 0

    cameraWidth = 8
    cameraHeight = 6

    path = []
    convexHull = []
    sbb = []
    area = sys.maxint

    def __init__(self, outline):
        self.outline = outline
        self.__findFlyOverPath()

    def __convertToUTM(self):
        for point in self.outline:
            self.utmOutline.append(UTMPoint(utm.from_latlon(point.lat, point.lon)))
        self.zone = self.utmOutline[0].zone
        self.zoneLetter = self.utmOutline[0].zoneLetter

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
        maxPt = UTMPoint((-sys.maxint-1, -sys.maxint-1, self.zone, self.zoneLetter))

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
                leftDir = edgeDirections[leftId]
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                topDir = UTMPoint((leftDir.n, -leftDir.e, self.zone, self.zoneLetter))
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                leftId = (leftId+1)%len(self.convexHull)
            elif smallestAngleIndex == 1: # right edge
                rightDir = edgeDirections[leftId]
                leftDir = UTMPoint((-rightDir.e, -rightDir.n, self.zone, self.zoneLetter))
                topDir = UTMPoint((leftDir.n, -leftDir.e, self.zone, self.zoneLetter))
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                rightId = (rightId+1)%len(self.convexHull)
            elif smallestAngleIndex == 2: # top edge
                topDir = edgeDirections[topId]
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                leftDir = UTMPoint((topDir.n, -topDir.e, self.zone, self.zoneLetter))
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                topId = (topId+1)%len(self.convexHull)
            elif smallestAngleIndex == 3: # bottom edge
                bottomDir = edgeDirections[bottomId]
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

            if area < self.area and area > 1:
                self.sbb = [upperLeft, upperRight, bottomLeft, bottomRight]
                self.area = area
                self.width = width
                self.height = height

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


if __name__ == "__main__":
    points = [geoPoint(38.84711546747433, -94.6733683347702), geoPoint(38.84703399781023, -94.67331871390343), geoPoint(38.847007885718675, -94.67337772250175),
              geoPoint(38.84698804052266, -94.67336967587471), geoPoint(38.84696192841424, -94.67344209551811), geoPoint(38.84706742127346, -94.67350110411644)]

    myHouse = house(points)
    convexHull = []
    sbb = []
    path = []

    for c in myHouse.convexHull:
        convexHull.append(c.toLatLon())

    for b in myHouse.sbb:
        sbb.append(b.toLatLon())

    for q in myHouse.path:
        path.append(q.toLatLon())

    print "original"
    printLatLong(points)

    print "convex"
    printLatLong(convexHull)

    print "sbb"
    printLatLong(sbb)

    print "path"
    printLatLong(path)

    print myHouse.area
