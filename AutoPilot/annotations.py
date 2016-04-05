
class RgbAnnotation():

    def __init__(self, image_num, origin, depth):
        self.image_num = image_num
        self.is_icedam = False
        self.origin = origin
        self.depth = depth
        self.ice_locations = []

class ThermalAnnotation():

    def __init__(self, num, is_hotspot):
        self.image_num = num
        self.hot_spot = is_hotspot
