from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from inspections import views

urlpatterns = [
    url(r'^create/$', views.InspectionsCreate.as_view()),
    url(r'^images/create/$', views.InspectionImagesCreate.as_view()),
    url(r'^get/$', views.InspectionsGet.as_view()),
    url(r'^images/get/$', views.InspectionImagesGet.as_view()),
    url(r'^image/icedam/$', views.InspectionImageIcedam.as_view()),
    url(r'^image/hotspot/$', views.InspectionImageHotspot.as_view()),
]

urlpatterns = format_suffix_patterns(urlpatterns)
