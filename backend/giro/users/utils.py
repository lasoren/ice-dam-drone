import hashlib
import random
import string
import time
import giro.short_url as short_url

def generate_session_id(email):
    """
    Generates a valid 128 character session_id for users to
    authenticate with
    """
    m = hashlib.sha512()
    m.update(''.join(random.choice(string.ascii_uppercase +
                                   string.digits) for _ in range(8)))
    m.update(email)
    m.update(str(time.time()))
    m.update("giro")
    return m.hexdigest()


def generate_confirmation_url(base_url, drone_operator_id):
    unique_code = short_url.encode_url(drone_operator_id)
    return base_url + unique_code, unique_code
