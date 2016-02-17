from users.serializers import DroneOperatorSerializer
from users.models import DroneOperator
import users.utils as users_utils

from django.contrib.auth import hashers
from rest_framework.views import APIView
from rest_framework.exceptions import APIException
from rest_framework.response import Response
from rest_framework import status


class RegisterDroneOperator(APIView):
    # Do NOT require authorization for this call since user does not
    # have user_id/session_id pair yet.
    permission_classes = ()

    """
    Endpoint for registering a new drone operator.
    """
    def post(self, request, format=None):
        request_data = request.DATA
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
            # TODO(luke): throw exception.
        except:
            pass

        serializer = DroneOperatorSerializer(request_data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data,
                status=status.HTTP_201_CREATED)
        return Response(serializer.errors)
