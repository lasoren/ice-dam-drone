from inspections.models import InspectionProvision

@transaction.atomic()
def add_to_inspection_provision(inspection_id):
    """
    Adds a row to the client provision, and deletes old rows
    """
    InspectionProvision.objects.filter(
        inspection_id=client_id).delete()
    provision = InspectionProvision.objects.create(
        inspection_id=client_id)
    provision.save()
