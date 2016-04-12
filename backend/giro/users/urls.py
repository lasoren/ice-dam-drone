from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from users import views

urlpatterns = [
    url(r'^register/$', views.RegisterDroneOperator.as_view()),
    url(r'^signin/$', views.SigninDroneOperator.as_view()),
    url(r'^client/create/$', views.ClientCreate.as_view()),
    url(r'^clients/get/$', views.ClientsGet.as_view()),
]

urlpatterns = format_suffix_patterns(urlpatterns)