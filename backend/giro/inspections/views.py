from giro import exceptions

from inspections.models import InspectionProvision
from inspections.models import InspectionImageProvision
from inspections.serializers import InspectionSerializer
from inspections.serializers import InspectionImageSerializer
import inspections.db_utils as inspections_db_utils
import inspections.utils as inspections_utils

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


class InspectionImagesCreate(APIView):
    """
    Endpoint for the android phone to list the images that were
    taken during an inspection run.
    """
    def post(self, request, format=None):
        inspection_images = request.data["inspection_images"]

        serializer = InspectionImageSerializer(data=inspection_images, many=True)
        if serializer.is_valid():
            serializer.save()
            # Add the images to the provision table for images.
            image_instances = serializer.instance
            image_ids = []
            for image_instance in image_instances:
                image_instance.path = inspections_utils.generate_image_path(
                    image_instance.inspection_id, image_instance.id)
                image_instance.save()
                image_ids.append(image_instance.id)
            inspections_db_utils.add_images_to_inspection_image_provision(image_ids)
            return Response(serializer.data,
                status=status.HTTP_201_CREATED)
        return Response(serializer.errors)

        # if "provision" not in request.data:
        #     raise exceptions.RequiredFieldMissing(
        #         'Provision field missing.')
        # try:
        #     next_provision = InspectionImageProvision.objects.latest('id').id + 1
        # except InspectionImageProvision.DoesNotExist:
        #     next_provision = 0
        # response = {'provision': next_provision}


class InspectionsGet(APIView):
    """
    Get all new or updated inspections for an operator account.
    """
    def post(self, request, format=None):
        if "provision" not in request.data:
            raise exceptions.RequiredFieldMissing(
                'Provision field missing.')
        try:
            next_provision = InspectionProvision.objects.latest('id').id + 1
        except InspectionProvision.DoesNotExist:
            next_provision = 0
        response = {'provision': next_provision}

        inspection_provisions = InspectionProvision.objects.filter(
            id__gt=request.data["provision"]
        ).select_related(
            'inspection', 'inspection__drone_operator'
        ).filter(inspection__drone_operator__pk=request.data["user_id"])

        inspections = []
        for inspection_provision in inspection_provisions:
            inspections.append(inspection_provision.inspection)
        response["inspections"] = InspectionSerializer(inspections, many=True).data
        return Response(response, status=status.HTTP_200_OK)
