from __future__ import unicode_literals

from django.db import models

from users.models import DroneOperator
from users.models import Client

# Image types collected by drone.
NOT_SPECIFIED = 1
RGB = 2
THERMAL = 3

IMAGE_TYPES = (
    (NOT_SPECIFIED, 'Not specified'),
    (RGB, 'RGB Image'),
    (THERMAL, 'Thermal Image')
)

# Treatment options for ice dam.
NOTHING_DONE = 1
SALTED_BY_DRONE = 2
POWER_WASHED = 3
ICE_PICKED = 4
OTHER = 5

TREATMENT_TYPES = (
    (NOTHING_DONE, 'Nothing done'),
    (SALTED_BY_DRONE, 'Drone left CaCl'),
    (POWER_WASHED, 'Power washed off roof'),
    (ICE_PICKED, 'Picked off roof manually'),
    (OTHER, 'Other treatment')
)

class Inspection(models.Model):
    """
    The service provided by an operator to a client. Involves
    drone inspecting exterior of roof for ice dams, checking roof
    of home for hotspots using thermal camera, and placing salt on
    affected areas to allievate the building pressure of the ice
    dam.
    """
    # Automatically added with new row.
    created = models.DateTimeField(auto_now_add=True)
    # The operator who initiated the inspection.
    drone_operator = models.ForeignKey(DroneOperator,
        related_name="drone_operator")
    # The client who is recieving home inspection.
    client = models.ForeignKey(Client)
    # If set, the inspection was not performed for some reason or
    # another.
    deleted = models.DateTimeField(blank=True, null=True)


class InspectionImage(models.Model):
    """
    SQL row, that represents an image collected by the drone
    and now stored on AWS bucket storage backend.
    """
    # Automatically added with new row. When this image was added
    # to the backend.
    created = models.DateTimeField(auto_now_add=True)
    # When this image was taken.
    taken = models.DateTimeField(auto_now_add=True)
    # Related field to the associated inspection from when this
    # image was collected.
    inspection = models.ForeignKey(Inspection)
    # Specifies whether the image is thermal, RGB, or not
    # specified.
    image_type = models.IntegerField(
        choices=IMAGE_TYPES, default=NOT_SPECIFIED)
    # The location of the image in bucket store on AWS.
    link = models.UrlField()
    # If set, the image was deleted for being poor quality, not
    # relevant, etc.
    deleted = models.DateTimeField(blank=True, null=True)


class IceDam(models.Model):
    """
    An ice dam that was identified by the drone during inspection
    and then confirmed by the operator.
    """
    # Automatically added with new row.
    created = models.DateTimeField(auto_now_add=True) 
    # If set, ice dam was misidentified or associated inspection
    # removed.  
    deleted = models.DateTimeField(blank=True, null=True)
    # The type of treatment that was done to fix this ice dam.
    treated = models.IntegerField(
        choices=TREATMENT_TYPES, default=NOTHING_DONE)
    # Associated inspection image for this ice dam. This allows us
    # to get inspection, as well as, see the image of this ice dam.
    inspection_image = models.ForeignKey(InspectionImage)


class Hotspot(models.Model):
    """
    A thermal hotspot that could cause future or current ice dam
    problems identified by the drone during inspection and
    confirmed by the operator.
    """
    # Automatically added with new row.
    created = models.DateTimeField(auto_now_add=True) 
    # If set, hotspot was misidentified or associated inspection
    # removed.
    deleted = models.DateTimeField(blank=True, null=True)  
    # Associated inspection image for this ice dam. This allows us
    # to get inspection, as well as, see the thermal image of this 
    # hotspot.
    inspection_image = models.ForeignKey(InspectionImage)
    