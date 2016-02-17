from django.conf.urls import patterns, url
from rest_framework.urlpatterns import format_suffix_patterns
from users import views

urlpatterns = patterns(
    'users.views',
    url(r'^register/$', views.RegisterDroneOperator.as_view()),
)

urlpatterns = format_suffix_patterns(urlpatterns)