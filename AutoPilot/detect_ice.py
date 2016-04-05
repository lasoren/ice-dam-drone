import threading, glob, os, subprocess, utm, jsonpickle
from saltplacement.kmeans_icicle_clusterer import KMeansIcicleClusterer
from house import UTMPoint

class DetectIce(threading.Thread):
    # haven't decided whether this class should be threaded or not
    # right now it isn't threaded
    command = ['./match_icicles', '', '']
    folder = os.path.join(os.path.expanduser('~'), 'ice-dam-drone', 'images', 'rgb_proc')

    def __init__(self, dir, annotations, centroid):
        """
        :param dir: directory of images to process
        :param annotations: array of data for each picture
        :param centroid: center of house
        """
        super(DetectIce, self).__init__()
        self.images = [str(file) for file in glob.glob("*.jpg")]
        os.makedirs(self.folder)
        self.annotations = annotations
        self.centroid = centroid
        self.__stopped = threading.Event()

    def run(self):
        self.__stopped.clear()
        for i in range(0, len(self.images)):
            self.command[1] = self.images[i]
            self.command[2] = self.folder + '/' + str(i) + '.jpg'

            proc = subprocess.Popen(self.command, stdout=subprocess.PIPE)
            data = proc.stdout.readline().strip().split(",")
            data.pop()
            data = [int(x) for x in data]

            ice_dams = KMeansIcicleClusterer(data).cluster()

            if len(ice_dams) > 0:
                self.__calculateIcePoints(ice_dams, self.annotations[i])

        os.chdir(self.folder)
        with open("images.json", "w+") as f:
            f.write(jsonpickle.encode(self.annotations, unpicklable=False, make_refs=False, keys=True))

    def __calculateIcePoints(self,ice_dams, annotation):
        """
        internal function
        calculates for each picture the ice dam locations and gives a real lat lon
        :param ice_dams: located x pos of ice dams on a picture, units of pixels
        :param annotation: data for this specific picture
        :return:
        """
        width = 3.70419 * annotation.depth # calculated using super accurate picture taking
        meter_per_pixel = width/1280/100
        origin = [float(x) for x in annotation.origin.split(",")]
        utm_origin = UTMPoint(utm.from_latlon(origin[0], origin[1]))

        # assumes that the drone is always perpendicular to the centroid of the house
        # we take the lidar measurement to the roof and turn the picture into a projection mapping that looks like a pyramid facing the house
        # using the pixel locations we can calculate how far left or right of the center of the projection map the ice dam is located
        for i in range(0, len(ice_dams)):
            vec = self.centroid.getVector(utm_origin).scalarMult(annotation.depth)
            x_loc = meter_per_pixel * ice_dams[i]
            x_offset = -1 * ((width/2) - x_loc) # left of the center is heading west, right is heading east
            vec.e += x_offset
            ice_loc = utm_origin.add(vec)
            annotation.ice_locations.append(ice_loc.toLatLon())

        annotation.is_icedam = True
