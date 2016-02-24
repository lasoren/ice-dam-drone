from rest_framework import serializers
from inspections.models import Inspection

class InspectionSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and out for a inspection object.
    """
    client_id = serializers.IntegerField(write_only=True)
    drone_operator_id = serializers.IntegerField(write_only=True)

    class Meta:
        model = User
        fields = (
            'id',
            'created',
            'client',
            'client_id',
            'drone_operator_id',
            'deleted',)
