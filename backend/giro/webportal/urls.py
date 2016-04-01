from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from webportal import views
import users.utils as users_utils
import inspections.utils as inspections_utils

urlpatterns = [
    url(r'^' + users_utils.EMAIL_PATH + '(?P<unique_code>.*)$', views.render_email_confirmation),
    url(r'^' + inspections_utils.PORTAL_PATH + '(?P<unique_code>.*)$', views.render_client_portal),
]

urlpatterns = format_suffix_patterns(urlpatterns)