import hashlib
import random
import string


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
