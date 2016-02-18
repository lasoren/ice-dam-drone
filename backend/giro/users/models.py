from __future__ import unicode_literals

from django.db import models

# Account status for drone operators.
NOT_SPECIFIED = 1
EMAIL_CONFIRMED = 2
DELETED = 3

ACCOUNT_STATUS_TYPES = (
    (NOT_SPECIFIED, 'Not specified'),
    (EMAIL_CONFIRMED, 'Email Confirmed'),
    (DELETED, 'Deleted')
)

class User(models.Model):
    """
    Generic user object storing data for operators, clients,
    or other future models.
    """
    # Automatically added with new row.
    created = models.DateTimeField(auto_now_add=True)
    # First name and last name split for convenience on
    # mobile side.
    first_name = models.TextField()
    last_name = models.TextField()
    # Email field automatically validates that an email is of
    # the correct format.
    email = models.EmailField(unique=True)


class DroneOperator(models.Model):
    """
    Operator of the Ice Dam Drone (Girodicer). Potentially the
    owner of an ice removal company or an employee of a
    municipality. Visits afflicted homes to offer inspection and 
    ice dam removal using the drone. Each inspection would be very
    low cost.
    """
    # Automatically added with new row.
    created = models.DateTimeField(auto_now_add=True)
    # The associated generic user in user table.
    user = models.OneToOneField(User)
    # Salted and SHA256 hashed before storage.
    password = models.CharField(max_length=256)
    # Session token for authentication.
    session_id = models.CharField(max_length=128)
    # Keeps track of the status of this account.
    deleted = models.IntegerField(choices=ACCOUNT_STATUS_TYPES,
        default=NOT_SPECIFIED)


class Client(models.Model):
    """
    A paying client of the Ice Dam Drone service. Client pays
    operator low cost to perform an inspection of his/her home.
    Client can review images with DroneOperator post-inspection or
    through online portal sent to their email. Clients don't need
    a password. They can access they're individual inspection
    using a unique, non-guessable link.
    """
    # Automatically added with new row.
    created = models.DateTimeField(auto_now_add=True)
    # The associated generic user in user table.
    user = models.OneToOneField(User)
    # The full text address of the client's home where the
    # inspection was performed.
    address = models.TextField()
    # When set to something other than null, this row has been
    # deleted and no longer returned to operator.
    deleted = models.DateTimeField(blank=True, null=True)
