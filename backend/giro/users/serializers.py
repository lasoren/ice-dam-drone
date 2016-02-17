from users.models import User, DroneOperator, Client
from rest_framework import serializers


class UserSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and out for a user object.
    """
    class Meta:
        model = User
        fields = (
            'created',
            'first_name',
            'last_name',
            'email',)


class DroneOperatorSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and output for a drone operator.
    """
    user = UserSerializer()

    class Meta:
        model = DroneOperator
        fields = (
            'created',
            'user',
            'password',
            'session_id',)


class ClientSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and output for a client of the drone.
    """
    user = UserSerializer()

    class Meta:
        model = Client
        fields = (
            'created',
            'user',
            'address',
            'deleted',)
