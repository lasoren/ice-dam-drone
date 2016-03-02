from django.db import transaction
from inspections.models import InspectionProvision

@transaction.atomic()
def add_to_inspection_provision(inspection_id):
    """
    Adds a row to the client provision, and deletes old rows
    """
    InspectionProvision.objects.filter(
        inspection_id=inspection_id).delete()
    InspectionProvision.objects.create(
        inspection_id=inspection_id)


@transaction.atomic()
def add_images_to_inspection_image_provision(inspection_image_ids):
    """
    Adds a list of rows to the inspection image provision
    and deletes old rows
    """
    InspectionImageProvision.objects.filter(
        inspection_image_id__in=inspection_image_ids).delete()
    inspection_image_provisions = [InspectionImageProvision(
        inspection_image_id=image_id)
        for image_id in inspection_image_ids]
    provision = InspectionImageProvision.objects.bulk_create(
        inspection_image_provisions)
