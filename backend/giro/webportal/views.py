from django.shortcuts import render
from users.models import EmailConfirmation
from users.models import EMAIL_CONFIRMED
from users.serializers import ClientSerializer
from users.serializers import DroneOperatorSerializer
from rest_framework.response import Response
from django.http import Http404
from inspections.models import IMAGE_TYPES
from inspections.models import Inspection
from inspections.models import InspectionImage
from inspections.serializers import InspectionSerializer
from inspections.serializers import InspectionImageSerializer


import datetime
import requests
import json


def render_email_confirmation(request, unique_code):
    try:
        email_confirmation = EmailConfirmation.objects.filter(
            unique_code=unique_code).select_related(
            'drone_operator', 'drone_operator__user')[0]
    except IndexError:
        raise Http404

    drone_operator = email_confirmation.drone_operator
    if email_confirmation.deleted == None:
        email_confirmation.deleted = datetime.datetime.utcnow()
        email_confirmation.save()
        # Set the drone operator as confirmed.
        drone_operator.status = EMAIL_CONFIRMED
        drone_operator.save()
    return render(request, "confirmation.html",
        DroneOperatorSerializer(drone_operator).data)


def render_client_portal(request, unique_code):
    # Find the inspection object this code belongs to.
    try:
        inspection = Inspection.objects.filter(
            deleted=None
        ).prefetch_related(
            'inspection_image'
        ).filter(
            inspection_image__path__startswith=unique_code
        ).distinct()[0]
    except IndexError:
        raise Http404

    render_dict = {}
    render_dict["inspection"] = InspectionSerializer(
        inspection).data
    render_dict["drone_operator"] = DroneOperatorSerializer(
        inspection.drone_operator).data
    render_dict["client"] = ClientSerializer(inspection.client).data
    # Get the images.
    images = InspectionImage.objects.filter(
        inspection_id=inspection.id, 
    ).prefetch_related(
        'icedam', 'hotspot'
    ).order_by(
        '-taken'
    ).distinct()
    # Sort images into image types.
    type_dict = {}
    image_dict = {}
    for pair in IMAGE_TYPES:
        type_dict[pair[0]] = pair[1]
        image_dict[pair[1]] = []
    for image in images:
        image_dict[type_dict[image.image_type]].append(
            InspectionImageSerializer(image).data)
    # Remove any keys that have no images.
    for key in image_dict.keys():
        if len(image_dict[key]) == 0:
            del image_dict[key]
    render_dict["images"] = json.dumps(image_dict)

    return render(request, "inspection.html", render_dict)







