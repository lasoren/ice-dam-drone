from django.conf.urls import patterns, include, url
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = patterns(
    '',
    # Examples:
    # url(r'^$', 'django_project.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),
    # url(r'^admin/', include(admin.site.urls)),
    url(r'^users/', include('users.urls')),
)
