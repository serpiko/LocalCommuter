from django.conf.urls.defaults import *
from piston.resource import Resource
from localcommuter.models import RutaLinea, Horarios 

#rutalinea_resource = Resource(RutaLineaHandler)
#horario_resource = Resource(HorarioHandler)

urlpatterns = patterns('',
#   url(r'^horarios/(?P<id>\d+)$', horario_resource),
#   url(r'^horarios$', horario_resource),
#   url(r'^rutalinea/(?P<id>\d+)$', rutalinea_resource),
#   url(r'^rutalinea$', rutalinea_resource),
)
