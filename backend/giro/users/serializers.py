from users.models import User, DroneOperator, Client
from rest_framework import serializers

import logging

class UserSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and out for a user object.
    """
    class Meta:
        model = User
        fields = (
            'id',
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
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        user_serializer = UserSerializer(data=validated_data["user"])
        if user_serializer.is_valid():
            user_serializer.save()
        del validated_data["user"]
        return DroneOperator.objects.create(user=user_serializer.instance, **validated_data)


class ClientSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and output for a client of the drone.
    """
    user = UserSerializer(read_only=True)
    user_id = serializers.IntegerField(write_only=True)

    class Meta:
        model = Client
        fields = (
            'created',
            'user',
            'user_id',
            'address',
            'deleted',)

