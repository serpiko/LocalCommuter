#-*- encoding: utf-8 -*-
from django.db import models
#from django.utils.translation import ugettext_lazi as _ 
#from smart_selects.db_fields import ChainedForeignKey

class SistemaTransporte(models.Model):
    _id = models.IntegerField(primary_key=True, db_column="_id")
    nombre = models.CharField(max_length=25)
    descripcion = models.TextField(max_length=250,blank=True)
    
    def __unicode__(self):
#        return u'%s' % self.get_nombre_display()
        return u'%s' % self.nombre
        

class Linea(models.Model):
    _id = models.IntegerField(primary_key=True, db_column="_id")
    name = models.CharField(max_length=25)
    sistematransporte = models.ForeignKey(SistemaTransporte)
    def __unicode__(self):
        return u'%s' % self.name

class  Parada(models.Model):
    _id = models.IntegerField(primary_key=True, db_column="_id")
    name = models.CharField(max_length=25)
    lat = models.FloatField()
    long = models.FloatField()
    def __unicode__(self):
        return u'%s' % self.name

class LineaTipoFrec(models.Model):
    _id = models.IntegerField(primary_key=True)
    numlinea = models.ForeignKey(Linea)
    tipofrec = models.CharField(max_length=25)
    frec = models.IntegerField()
    def __unicode__(self):
        return u'%s' % self.tipofrec

class RutaLinea(models.Model):
    _id = models.IntegerField(primary_key=True)
    numlinea = models.ForeignKey(Linea)
    numparada = models.ForeignKey(Parada)
    orden = models.IntegerField()
    sentido = models.IntegerField()

class Horarios(models.Model):
    _id = models.AutoField(primary_key=True)
    numlinea = models.ForeignKey(Linea)
    numparada = models.ForeignKey(Parada)
    fecha = models.DateField()
    gpx_user = models.FloatField()
    gpy_user = models.FloatField()

    def save(self, *args, **kwargs):
        if self.fecha == None:
            self.fecha = datetime.now()
        super(Case, self).save(*args, **kwargs)

    
