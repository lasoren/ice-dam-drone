
UNIDENTIFIED = 1
RGB = 2
THERMAL = 3
AERIAL = 4

class Annotation():

    def __init__(self, image_num=None, origin=None, depth=None, type=0, detected=False):
        self.image_num = image_num
        self.image_type = type
        self.detected = detected
        self.origin = origin
        self.depth = depth
        self.ice_locations = []
