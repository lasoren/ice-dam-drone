from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from inspections import views

urlpatterns = [
    url(r'^create/$', views.InspectionsCreate.as_view()),
]

urlpatterns = format_suffix_patterns(urlpatterns)
