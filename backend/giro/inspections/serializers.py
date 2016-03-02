from rest_framework import serializers
from inspections.models import Inspection
from inspections.models import InspectionImage
from users.serializers import ClientSerializer

class InspectionSerializer(serializers.ModelSerializer):
    """
    Serializes JSON input and out for a inspection object.
    """
    client_id = serializers.IntegerField(write_only=True)
    drone_operator_id = serializers.IntegerField(write_only=True)
    client = ClientSerializer(read_only=True)

    class Meta:
        model = Inspection
        fields = (
            'id',
            'created',
            'client',
            'client_id',
            'drone_operator_id',
            'deleted',)


class InspectionImageSerializer(serializers.ModelSerializer):
    """
    Serializers JSON input and out for image objects.
    """
    inspection_id = serializers.IntegerField()

    class Meta:
        model = InspectionImage
        fields = (
            'id',
            'created',
            'taken',
            'inspection_id',
            'image_type',
            'link',
            'deleted',)
