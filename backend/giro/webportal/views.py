from django.shortcuts import render
from users.models import EmailConfirmation
from users.models import EMAIL_CONFIRMED
from users.serializers import DroneOperatorSerializer
from rest_framework.response import Response
from django.http import Http404

import datetime
import requests

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
