import math

class geoPoint:
    lon = 0
    lat = 0

    def __init__(self, lon, lat):
        self.lon = lon
        self.lat = lat

class box:

    def __init__(self, index):
        self.corners = [index, index, index, index]
        self.axisX = None
        self.axisY = None

class house:
    __scale = 10E10 # used to scale the floating point values so they don't get truncated during calculation

    def __init__(self, outline):
        self.outline = outline
        self.convexHull = [0, 0, 0, 0]
        self.__findConvexHull()
        self.minBox = self.__findMinimumBox()

    def __findMinimumBox(self): # using algorithm as described in this paper: http://www.geometrictools.com/Documentation/MinimumAreaRectangle.pdf
        if len(self.convexHull) == 0:
            return

        numPoints = len(self.convexHull)
        mBox = box(numPoints-1)
        mBox.axisX = self.__findVector(self.convexHull[0], self.convexHull[numPoints - 1])
        mBox.axisY = geoPoint(mBox.axisX.lat, -mBox.axisX.lon)

        origin = self.convexHull[numPoints - 1]
        zero = geoPoint(0, 0)
        support = [zero, zero, zero, zero]

        for i in range(0, len(self.convexHull)):
            diff = self.__vectorDiff(origin, self.convexHull[i])
            v = geoPoint(self.__dot(mBox.axisX, diff), self.__dot(mBox.axisY, diff))

            if (v.lon > support[1].lon) or (v.lon == support[1].lon and v.lat > support[1].lat):
                mBox.corners[1] = i
                support[1] = v

            if (v.lat > support[2].lat) or (v.lat == support[2].lat and v.lon < support[2].lon):
                mBox.corners[2] = i
                support[2] = v

            if (v.lon > support[3].lon) or (v.lon == support[3].lon and v.lat < support[3].lat):
                mBox.corners[3] = i
                support[3] = v

        for i in range(0, len(mBox.corners)):
            mBox.corners[i] = geoPoint(mBox.corners[i].lon / self.__scale, mBox.corners[i].lat / self.__scale)

        return mBox

    def __findVector(self, geoPoint1, geoPoint2):
        lon = geoPoint2.lon - geoPoint1.lon
        lat = geoPoint2.lat - geoPoint1.lat

        mag = math.sqrt( math.pow(lon, 2) + math.pow(lat,2) )

        return geoPoint(lon/mag, lat/mag)

    def __vectorDiff(self, vec1, vec2):
        return geoPoint(vec2.lon - vec1.lon, vec2.lat - vec1.lat)

    def __dot(self, vec1, vec2):
        return (vec1.lon * vec2.lon) + (vec1.lat * vec2.lat)

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
        for i in range(0, len(self.convexHull)):
            self.convexHull[i] = geoPoint(self.convexHull[i].lon * self.__scale, self.convexHull[i].lat * self.__scale)

    def __findHullPoints(self, point):
        self.convexHull.append(self.outline[point])

        ccwPoint = (point+1) % len(self.outline)

        for i in range(0, len(self.outline)):
            if self.__isPointCCW(self.outline[point], self.outline[i], self.outline[ccwPoint]):
                ccwPoint = i

        return ccwPoint

    def __isPointCCW(self, p1, p2, p3):
        val = ((p2.lat - p1.lat) * (p3.lon - p2.lon)) - ((p2.lon - p1.lon) * (p3.lat - p2.lat))
        return True if (val < 0) else False

if __name__ == "__main__":
    points = [geoPoint(38.84711546747433, -94.6733683347702), geoPoint(38.84703399781023, -94.67331871390343), geoPoint(38.847007885718675, -94.67337772250175),
              geoPoint(38.84698804052266, -94.67336967587471), geoPoint(38.84696192841424, -94.67344209551811), geoPoint(38.84706742127346, -94.67350110411644)]

    #points = [geoPoint(1,1), geoPoint(5,3), geoPoint(3.5, 2)]

    myHouse = house(points)
    print myHouse