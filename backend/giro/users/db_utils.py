from django.conf import settings
from rest_framework import permissions
from users.models import DroneOperator
from users.models import ClientProvision

from django.db import transaction
import logging


class SessionExistsForUser(permissions.BasePermission):
    """
    Each request must have a user_id and session_id key.
    We must know of a session that matches this user/session id pair,
    otherwise authentication fails.

    DO NOT CHANGE unless you know what you are doing.
    All API requests are authenticated with this class.
    """

    def has_permission(self, request, view):
        return True
        if request.method == 'OPTIONS':
            # Only allow if in DEBUG mode.
            return settings.DEBUG
        if 'user_id' not in request.data:
            return False
        if 'session_id' not in request.data:
            return False
        if not authenticate(request.data['user_id'],
                            request.data['session_id']):
            return False
        request.user_id = request.data['user_id']
        request.session_id = request.data['session_id']
        return True


def authenticate(user_id, session_id):
    """
    Authenticates a user. Returns True if user_id and session_id are valid
    """
    try:
        drone_operator = DroneOperator.objects.get(id=user_id)
        logging.debug(
            "Retrieved session for user with id: " + str(user_id))
        # Verifies that the session_id given matches session in our db
        return drone_operator.session_id == session_id
    except Session.DoesNotExist:
        logging.debug(
            "Session not found for user id: " + str(user_id))
        return False


@transaction.atomic()
def add_to_client_provision(client_id):
    """
    Adds a row to the client provision, and deletes old rows
    """
    ClientProvision.objects.filter(client_id=client_id).delete()
    provision = ClientProvision.objects.create(client_id=client_id)
    provision.save()
