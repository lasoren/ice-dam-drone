from giro import exceptions
from users.serializers import DroneOperatorSerializer
from users.models import DroneOperator
import users.utils as users_utils

from django.contrib.auth import hashers
from rest_framework.views import APIView
from rest_framework.exceptions import APIException
from rest_framework.response import Response
from rest_framework import status

import logging


class RegisterDroneOperator(APIView):
    # Do NOT require authorization for this call since user does not
    # have user_id/session_id pair yet.
    permission_classes = ()

    """
    Endpoint for registering a new drone operator.
    """
    def post(self, request, format=None):
        request_data = request.data
        # Hash the provided password.
        request_data["password"] = hashers.make_password(
            request_data["password"])
        email = request_data["user"]["email"]
        # Generate a random session_id for this operator.
        request_data["session_id"] = users_utils.generate_session_id(
            email)

        try:
            drone_operator = DroneOperator.objects.get(user__email=email)
            # Operator account already exists. Throw exception. Operator
            # should use reset password if password is forgotten.
            raise exceptions.OperatorExists(
                'An operator with this email already exists.')
        except DroneOperator.DoesNotExist:
            pass

        serializer = DroneOperatorSerializer(data=request_data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data,
                status=status.HTTP_201_CREATED)
        return Response(serializer.errors)


class SigninDroneOperator(APIView):
    # Do NOT require authorization for this call since user does not
    # have user_id/session_id pair yet.
    permission_classes = ()

    """
    Endpoint for signing in as a drone operator and getting a session id.
    """
    





