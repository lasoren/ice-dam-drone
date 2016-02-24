from giro import exceptions
from users.serializers import DroneOperatorSerializer
from users.serializers import ClientSerializer
from users.serializers import UserSerializer
from users.models import DroneOperator
from users.models import Client
from users.models import User
from users.models import ClientProvision
from users.models import EMAIL_CONFIRMED
import users.utils as users_utils
import db_utils as users_db_utils

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
            # Send out confirmation email to drone operator
            # for them to confirm their account.
            users_db_utils.send_confirmation_email(
                request.get_host(), serializer.instance)
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
    def post(self, request, format=None):
        request_data = request.data
        email = request_data["email"]

        try:
            drone_operator = DroneOperator.objects.get(
                user__email=email,
                status=EMAIL_CONFIRMED)
        except DroneOperator.DoesNotExist:
            raise exceptions.OperatorAccountInvalid(
                'No account for this email or ' +
                'email not confirmed for this account.')

        if hashers.check_password(request_data["password"], drone_operator.password):
            return Response(DroneOperatorSerializer(drone_operator).data)
        raise exceptions.PasswordInvalid(
            'Password is incorrect for this account.')


class ClientCreate(APIView):
    """
    Endpoint for a drone operator to create a client.
    """
    def post(self, request, format=None):
        request_data = request.data
        client_data = request_data["client"]
        try:
            client = Client.objects.get(user__email=client_data["user"]["email"])
            user_serializer = UserSerializer(client.user, data=client_data["user"])
        except Client.DoesNotExist:
            client = None
            user_serializer = UserSerializer(data=client_data["user"])
        # Update information for user.
        if user_serializer.is_valid():
            user_serializer.save()
        else: 
            return Response(user_serializer.errors)
        del client_data["user"]
        client_data["user_id"] = user_serializer.instance.id
        # Update information for client.
        if client:
            serializer = ClientSerializer(client, data=client_data)
        else:
            serializer = ClientSerializer(data=client_data)

        if serializer.is_valid():
            serializer.save()
            # Add this client to the provision table.
            users_db_utils.add_to_client_provision(
                serializer.instance.id)
            return Response(serializer.data,
                status=status.HTTP_201_CREATED)
        return Response(serializer.errors)


class ClientsGet(APIView):
    """
    Get my past clients, as a operator, sorted by creation date.
    """
    def post(self, request, format=None):
        if "provision" not in request.data:
            raise exceptions.RequiredFieldMissing(
                'Provision field missing.')
        try:
            next_provision = ClientProvision.objects.latest('id').id + 1
        except MatchProvision.DoesNotExist:
            next_provision = 0
        response = {'provision': next_provision}

        client_provisions = ClientProvision.objects.filter(
            id__gt=request.data["provision"]
        ).select_related(
            'client'
        ).prefetch_related(
            'client__inspection_client__drone_operator'
        ).select_related(
            'client__user'
        ).filter(
            client__inspection_client__drone_operator__pk=request.data["user_id"],
            client__deleted=None  # Client record has not been deleted.
        ).order_by(  # Order by the clients that have been updated recently.
            '-timestamp'
        ).distinct()

        clients = []
        for client_provision in client_provisions:
            clients.append(client_provision.client)
        response["clients"] = ClientSerializer(clients, many=True).data
        return Response(response, status=status.HTTP_200_OK)
