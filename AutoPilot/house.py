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


class box:

    def __init__(self, index):
        self.corners = [index, index, index, index]
        self.axisX = None
        self.axisY = None

class house:
    utmOutline = []
    zone = 0
    zoneLetter = ""

    def __init__(self, outline):
        self.outline = outline
        self.__convertToUTM()
        self.convexHull = [0, 0, 0, 0]
        self.sbb = []
        self.area = sys.maxint
        self.__findConvexHull()
        self.__findMinimumBox()

    def __convertToUTM(self):
        for point in self.outline:
            self.utmOutline.append(UTMPoint(utm.from_latlon(point.lat, point.lon)))
        self.zone = self.utmOutline[0].zone
        self.zoneLetter = self.utmOutline[0].zoneLetter

    def __findMinimumBox(self): # rotating calipers as described here: https://geidav.wordpress.com/2014/01/23/computing-oriented-minimum-bounding-boxes-in-2d/
        edgeDirections = []

        for i in range(0, len(self.convexHull)):
            edgeDirections.append(self.convexHull[i].getVector(self.convexHull[(i+1)%len(self.convexHull)]))

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

        leftDir = UTMPoint((0.0, -1.0, self.zone, self.zoneLetter))
        rightDir = UTMPoint((0.0, 1.0, self.zone, self.zoneLetter))
        topDir = UTMPoint((-1.0, 0.0, self.zone, self.zoneLetter))
        bottomDir = UTMPoint((1.0, 0.0, self.zone, self.zoneLetter))

        for i in range(0, len(self.convexHull)):
            angles = [math.acos(edgeDirections[leftId].dot(leftDir)),
                      math.acos(edgeDirections[rightId].dot(rightDir)),
                      math.acos(edgeDirections[topId].dot(topDir)),
                      math.acos(edgeDirections[bottomId].dot(bottomDir))]

            smallestAngleIndex = angles.index(min(angles))

            if smallestAngleIndex == 0:
                leftDir = edgeDirections[leftId]
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                topDir = UTMPoint((leftDir.n, -leftDir.e, self.zone, self.zoneLetter))
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                leftId = (leftId+1)%len(self.convexHull)
            elif smallestAngleIndex == 1:
                rightDir = edgeDirections[leftId]
                leftDir = UTMPoint((-rightDir.e, -rightDir.n, self.zone, self.zoneLetter))
                topDir = UTMPoint((leftDir.n, -leftDir.e, self.zone, self.zoneLetter))
                bottomDir = UTMPoint((-topDir.e, -topDir.e, self.zone, self.zoneLetter))
                rightId = (rightId+1)%len(self.convexHull)
            elif smallestAngleIndex == 2:
                topDir = edgeDirections[topId]
                bottomDir = UTMPoint((-topDir.e, -topDir.n, self.zone, self.zoneLetter))
                leftDir = UTMPoint((topDir.n, -topDir.e, self.zone, self.zoneLetter))
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                topId = (topId+1)%len(self.convexHull)
            elif smallestAngleIndex == 3:
                bottomDir = edgeDirections[bottomId]
                topDir = UTMPoint((-bottomDir.e, -bottomDir.n, self.zone, self.zoneLetter))
                leftDir = UTMPoint((bottomDir.n, -bottomDir.e, self.zone, self.zoneLetter))
                rightDir = UTMPoint((-leftDir.e, -leftDir.n, self.zone, self.zoneLetter))
                bottomId = (bottomId+1)%len(self.convexHull)

            upperLeft = self.__calcLineIntersect(self.convexHull[leftId], leftDir, self.convexHull[topId], topDir)
            upperRight = self.__calcLineIntersect(self.convexHull[rightId], rightDir, self.convexHull[topId], topDir)
            bottomLeft = self.__calcLineIntersect(self.convexHull[bottomId], bottomDir, self.convexHull[leftId], leftDir)
            bottomRight = self.__calcLineIntersect(self.convexHull[bottomId], bottomDir, self.convexHull[rightId], rightDir)

            width = upperLeft.dist(upperRight)
            height = upperLeft.dist(bottomLeft)

            area = width*height

            if area < self.area and area > 1:
                self.sbb = [UTMPoint((upperLeft.e, upperLeft.n, self.zone, self.zoneLetter)),
                            UTMPoint((upperRight.e, upperRight.n, self.zone, self.zoneLetter)),
                            UTMPoint((bottomLeft.e, bottomLeft.n, self.zone, self.zoneLetter)),
                            UTMPoint((bottomRight.e, bottomRight.n, self.zone, self.zoneLetter))]
                self.area = area

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

if __name__ == "__main__":
    points = [geoPoint(38.84711546747433, -94.6733683347702), geoPoint(38.84703399781023, -94.67331871390343), geoPoint(38.847007885718675, -94.67337772250175),
              geoPoint(38.84698804052266, -94.67336967587471), geoPoint(38.84696192841424, -94.67344209551811), geoPoint(38.84706742127346, -94.67350110411644)]

    myHouse = house(points)
    convexHull = []
    sbb = []

    for c in myHouse.convexHull:
        convexHull.append(c.toLatLon())

    for b in myHouse.sbb:
        sbb.append(b.toLatLon())

    printLatLong(sbb)

    print myHouse.area
