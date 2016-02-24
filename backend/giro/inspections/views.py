from giro import exceptions

from inspections.serializers import InspectionSerializer

from rest_framework.views import APIView
from rest_framework.exceptions import APIException
from rest_framework.response import Response
from rest_framework import status

class InspectionsCreate(APIView):
    """
    Endpoint for a drone operator to create an inspection
    for a client.
    """
    def post(self, request, format=None):
        inspection_data = request.data["inspection"]

        serializer = InspectionSerializer(data=inspection_data)
        if serializer.is_valid():
            serializer.save()
            # Add the inspection to the provision table.
            inspections_db_utils.add_to_inspection_provision(
                serializer.instance.id)
            return Response(serializer.data,
                status=status.HTTP_201_CREATED)
        return Response(serializer.errors)
