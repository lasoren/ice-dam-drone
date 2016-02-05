class geoPoint:
    lon = 0
    lat = 0

    def __init__(self, lon, lat):
        self.lon = lon
        self.lat = lat

class house:

    def __init__(self, outline):
        self.outline = outline
        self.convexHull = []
        self.__findConvexHull()

    def __findConvexHull(self): # using the gift wrapping algorithm
        self.convexHull = []
        # need to grab the left
        mlPointIndex = 0
        for i in range(0, len(self.outline)):
            if self.outline[i].lon < self.outline[mlPointIndex].lon:
                mlPointIndex = i

        i = self.__findHullPoints(mlPointIndex)

        while i != mlPointIndex:
            i = self.__findHullPoints(i)

    def __findHullPoints(self, point):
        self.convexHull.append(self.outline[point])

        ccwPoint = (point+1) % len(self.outline)

        for i in range(0, len(self.outline)):
            if self.__findThreePointOrientation(self.outline[point], self.outline[i], self.outline[ccwPoint]) == 2:
                ccwPoint = i

        return ccwPoint

    def __findThreePointOrientation(self, p1, p2, p3):
        val = (p2.lat - p1.lat) * (p3.lon - p2.lon) - (p2.lon - p1.lon) * (p3.lat - p2.lat)

        if val == 0:
            return 0

        return 1 if (val > 0) else 2

if __name__ == "__main__":
    points = [geoPoint(38.84711546747433, -94.6733683347702), geoPoint(38.84703399781023, -94.67331871390343), geoPoint(38.847007885718675, -94.67337772250175),
              geoPoint(38.84698804052266, -94.67336967587471), geoPoint(38.84696192841424, -94.67344209551811), geoPoint(38.84706742127346, -94.67350110411644)]

    myHouse = house(points)