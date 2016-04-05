
import giro.short_url as short_url

PORTAL_PATH = "inspection/"

def generate_image_path(inspection_id, inspection_image_id):
    inspection_code = short_url.encode_url(inspection_id)
    image_code = short_url.encode_url(inspection_image_id)
    return inspection_code + '/' + image_code