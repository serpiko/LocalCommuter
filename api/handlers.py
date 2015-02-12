from piston.handler import BaseHandler
from localcommuter.models import RutaLinea, Horarios

class RutaLineaHandler(BaseHandler):
    allowed_methods = ('GET',)
    model = RutaLinea
    def read(self, request):
        """
        devuelve el conjunto de rutas actualizados 

        """
        base = RutaLinea.objects

class HorariosHandler(BaseHandler):
    allowed_methods = ('GET',)
    model = Horarios
    
    def read(self, request):
        """
        devuelve el conjunto de horarios actualizados 

        """
        base = Horarios.objects
        return base.all()
