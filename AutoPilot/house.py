import math, sys

def printLatLong(list):
    for item in list:
        print "%.10f, %.10f" % (item.lat, item.lon)

class geoPoint:
    lon = 0
    lat = 0

    def __init__(self, lat, lon):
        self.lon = lon
        self.lat = lat

class box:

    def __init__(self, index):
        self.corners = [index, index, index, index]
        self.axisX = None
        self.axisY = None

class house:
    __scale = 1 # used to scale the floating point values so they don't get truncated during calculation

    def __init__(self, outline):
        self.outline = outline
        self.convexHull = [0, 0, 0, 0]
        self.sbb = []
        self.__findConvexHull()
        self.__findMinimumBox()

    def __findMinimumBox(self): # rotating calipers as described here: https://geidav.wordpress.com/2014/01/23/computing-oriented-minimum-bounding-boxes-in-2d/
        edgeDirections = []

        for i in range(0, len(self.convexHull)):
            diff = self.__vectorDiff(self.convexHull[(i+1)%len(self.convexHull)], self.convexHull[i])
            mag = math.sqrt( math.pow(diff.lon, 2) + math.pow(diff.lat,2) )
            edgeDirections.append(geoPoint(diff.lat / mag, diff.lon / mag))

        leftId = None
        rightId = None
        topId = None
        bottomId = None
        minPt = geoPoint(sys.maxint, sys.maxint)
        maxPt = geoPoint(-sys.maxint - 1, -sys.maxint - 1)

        for i in range(0, len(self.convexHull)):
            point = self.convexHull[i]
            if point.lon < minPt.lon:
                minPt.lon = point.lon
                leftId = i

            if point.lat < minPt.lat:
                minPt.lat = point.lat
                bottomId = i

            if point.lon > maxPt.lon:
                maxPt.lon = point.lon
                rightId = i

            if point.lat > maxPt.lat:
                maxPt.lat = point.lat
                topId = i

        leftDir = geoPoint(-1.0, 0.0);
        rightDir = geoPoint(1.0, 0.0);
        topDir = geoPoint(0.0, -1.0);
        bottomDir = geoPoint(0.0, 1.0);

        bestArea = sys.maxint

        for i in range(0, len(self.convexHull)):
            print "Lat %f Lon %f" % (leftDir.lat, leftDir.lon)
            print "edgeDir: lat %f lon %d" % (edgeDirections[leftId].lat, edgeDirections[leftId].lon)
            print "LeftDot %f" % self.__dot(leftDir, edgeDirections[leftId])
            angles = [math.acos(self.__dot(leftDir, edgeDirections[leftId])),
                      math.acos(self.__dot(rightDir, edgeDirections[rightId])),
                      math.acos(self.__dot(topDir, edgeDirections[topId])),
                      math.acos(self.__dot(bottomDir, edgeDirections[bottomId]))]

            smallestAngleIndex = angles.index(min(angles))
            print "Smallest Angle Index %d" % smallestAngleIndex

            if smallestAngleIndex == 0:
                leftDir = edgeDirections[leftId]
                rightDir = geoPoint(-leftDir.lat, -leftDir.lon)
                topDir = geoPoint(-leftDir.lon, leftDir.lat)
                bottomDir = geoPoint(-topDir.lat, -topDir.lon)
                leftId = (leftId+1)%len(self.convexHull)
            elif smallestAngleIndex == 1:
                rightDir = edgeDirections[leftId]
                leftDir = geoPoint(-rightDir.lat, -rightDir.lon)
                topDir = geoPoint(-leftDir.lon, leftDir.lat)
                bottomDir = geoPoint(-topDir.lat, -topDir.lon)
                rightId = (rightId+1)%len(self.convexHull)
            elif smallestAngleIndex == 2:
                topDir = edgeDirections[topId]
                bottomDir = geoPoint(-topDir.lat, -topDir.lon)
                leftDir = geoPoint(-topDir.lon, topDir.lat)
                rightDir = geoPoint(-leftDir.lat, -leftDir.lon)
                topId = (topId+1)%len(self.convexHull)
            elif smallestAngleIndex == 3:
                bottomDir = edgeDirections[bottomId]
                topDir = geoPoint(-bottomDir.lat, -bottomDir.lon)
                leftDir = geoPoint(-bottomDir.lon, bottomDir.lat)
                rightDir = geoPoint(-leftDir.lat, -leftDir.lon)
                bottomId = (bottomId+1)%len(self.convexHull)

            upperLeft = self.__calcLineIntersect(self.convexHull[leftId], leftDir, self.convexHull[topId], topDir)
            upperRight = self.__calcLineIntersect(self.convexHull[rightId], rightDir, self.convexHull[topId], topDir)
            bottomLeft = self.__calcLineIntersect(self.convexHull[bottomId], bottomDir, self.convexHull[leftId], leftDir)
            bottomRight = self.__calcLineIntersect(self.convexHull[bottomId], bottomDir, self.convexHull[rightId], rightDir)

            width = self.__vectorDistance(upperRight, upperLeft)
            height = self.__vectorDistance(bottomLeft, upperLeft)

            area = width*height

            if area < bestArea:
                self.sbb = [geoPoint(upperLeft.lat/self.__scale, upperLeft.lon/self.__scale),
                            geoPoint(upperRight.lat/self.__scale, upperRight.lon/self.__scale),
                            geoPoint(bottomLeft.lat/self.__scale, bottomLeft.lon/self.__scale),
                            geoPoint(bottomRight.lat/self.__scale, bottomRight.lon/self.__scale)]
                #printLatLong(self.sbb)
                bestArea = area

    def __findVector(self, geoPoint1, geoPoint2):
        lon = geoPoint2.lon - geoPoint1.lon
        lat = geoPoint2.lat - geoPoint1.lat

        diff = self.__vectorDiff(geoPoint1, geoPoint2)
        mag = math.sqrt( math.pow(diff.lon, 2) + math.pow(diff.lat,2) )

        return geoPoint(lat/mag, lon/mag)

    def __vectorDiff(self, vec1, vec2):
        return geoPoint(vec2.lat - vec1.lat, vec2.lon - vec1.lon)

    def __dot(self, vec1, vec2):
        dot = (vec1.lon * vec2.lon) + (vec1.lat * vec2.lat)
        if dot < -1:
            return -1.0
        elif dot > 1:
            return 1.0
        return dot

    def __vectorDistance(self, vec1, vec2):
        lon = vec2.lon - vec1.lon
        lat = vec2.lat - vec1.lat
        return math.sqrt(math.pow(lon, 2) + math.pow(lat, 2))

    def __findConvexHull(self): # uses monotone-chain algorithm
        sortedOutline = sorted(self.outline, key=lambda x: x.lon)

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
        self.convexHull.reverse()
        for i in range(0, len(self.convexHull)):
            self.convexHull[i] = geoPoint(self.convexHull[i].lat * self.__scale, self.convexHull[i].lon * self.__scale)

    def __isPointCCW(self, p1, p2, p3):
        val = ((p2.lon - p1.lon) * (p3.lat - p2.lat)) - ((p2.lat - p1.lat) * (p3.lon - p2.lon))
        return True if (val < 0) else False

    def __calcLineIntersect(self, line1, dir1, line2, dir2):
        dd = (dir1.lon*dir2.lat) - (dir1.lat*dir2.lon)
        dx = line2.lon - line1.lon
        dy = line2.lat - line1.lat
        t = ((dx * dir2.lat) - (dy * dir2.lon))/dd

        return geoPoint((line1.lat+ (t*dir1.lat)), (line1.lon + (t*dir1.lon)))

if __name__ == "__main__":
    points = [geoPoint(38.84711546747433, -94.6733683347702), geoPoint(38.84703399781023, -94.67331871390343), geoPoint(38.847007885718675, -94.67337772250175),
              geoPoint(38.84698804052266, -94.67336967587471), geoPoint(38.84696192841424, -94.67344209551811), geoPoint(38.84706742127346, -94.67350110411644)]

    #points = [geoPoint(1,1), geoPoint(5,3), geoPoint(3.5, 2)]

    myHouse = house(points)
    #printLatLong(myHouse.sbb)
    printLatLong(myHouse.convexHull)