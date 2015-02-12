# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'SistemaTransporte'
        db.create_table(u'localcommuter_sistematransporte', (
            ('_id', self.gf('django.db.models.fields.IntegerField')(primary_key=True, db_column='_id')),
            ('nombre', self.gf('django.db.models.fields.CharField')(max_length=25)),
            ('descripcion', self.gf('django.db.models.fields.TextField')(max_length=250, blank=True)),
        ))
        db.send_create_signal(u'localcommuter', ['SistemaTransporte'])

        # Adding model 'Linea'
        db.create_table(u'localcommuter_linea', (
            ('_id', self.gf('django.db.models.fields.IntegerField')(primary_key=True, db_column='_id')),
            ('name', self.gf('django.db.models.fields.CharField')(max_length=25)),
            ('sistematransporte', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['localcommuter.SistemaTransporte'])),
        ))
        db.send_create_signal(u'localcommuter', ['Linea'])

        # Adding model 'Parada'
        db.create_table(u'localcommuter_parada', (
            ('_id', self.gf('django.db.models.fields.IntegerField')(primary_key=True, db_column='_id')),
            ('name', self.gf('django.db.models.fields.CharField')(max_length=25)),
            ('lat', self.gf('django.db.models.fields.FloatField')()),
            ('long', self.gf('django.db.models.fields.FloatField')()),
        ))
        db.send_create_signal(u'localcommuter', ['Parada'])

        # Adding model 'LineaTipoFrec'
        db.create_table(u'localcommuter_lineatipofrec', (
            ('_id', self.gf('django.db.models.fields.IntegerField')(primary_key=True)),
            ('numlinea', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['localcommuter.Linea'])),
            ('tipofrec', self.gf('django.db.models.fields.CharField')(max_length=25)),
            ('frec', self.gf('django.db.models.fields.IntegerField')()),
        ))
        db.send_create_signal(u'localcommuter', ['LineaTipoFrec'])

        # Adding model 'RutaLinea'
        db.create_table(u'localcommuter_rutalinea', (
            ('_id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('numlinea', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['localcommuter.Linea'])),
            ('numparada', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['localcommuter.Parada'])),
            ('orden', self.gf('django.db.models.fields.IntegerField')()),
            ('sentido', self.gf('django.db.models.fields.IntegerField')()),
        ))
        db.send_create_signal(u'localcommuter', ['RutaLinea'])

        # Adding model 'Horarios'
        db.create_table(u'localcommuter_horarios', (
            ('_id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('numlinea', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['localcommuter.Linea'])),
            ('numparada', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['localcommuter.Parada'])),
            ('fecha', self.gf('django.db.models.fields.DateField')()),
            ('gpx_user', self.gf('django.db.models.fields.FloatField')()),
            ('gpy_user', self.gf('django.db.models.fields.FloatField')()),
        ))
        db.send_create_signal(u'localcommuter', ['Horarios'])


    def backwards(self, orm):
        # Deleting model 'SistemaTransporte'
        db.delete_table(u'localcommuter_sistematransporte')

        # Deleting model 'Linea'
        db.delete_table(u'localcommuter_linea')

        # Deleting model 'Parada'
        db.delete_table(u'localcommuter_parada')

        # Deleting model 'LineaTipoFrec'
        db.delete_table(u'localcommuter_lineatipofrec')

        # Deleting model 'RutaLinea'
        db.delete_table(u'localcommuter_rutalinea')

        # Deleting model 'Horarios'
        db.delete_table(u'localcommuter_horarios')


    models = {
        u'localcommuter.horarios': {
            'Meta': {'object_name': 'Horarios'},
            '_id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'fecha': ('django.db.models.fields.DateField', [], {}),
            'gpx_user': ('django.db.models.fields.FloatField', [], {}),
            'gpy_user': ('django.db.models.fields.FloatField', [], {}),
            'numlinea': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['localcommuter.Linea']"}),
            'numparada': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['localcommuter.Parada']"})
        },
        u'localcommuter.linea': {
            'Meta': {'object_name': 'Linea'},
            '_id': ('django.db.models.fields.IntegerField', [], {'primary_key': 'True', 'db_column': "'_id'"}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '25'}),
            'sistematransporte': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['localcommuter.SistemaTransporte']"})
        },
        u'localcommuter.lineatipofrec': {
            'Meta': {'object_name': 'LineaTipoFrec'},
            '_id': ('django.db.models.fields.IntegerField', [], {'primary_key': 'True'}),
            'frec': ('django.db.models.fields.IntegerField', [], {}),
            'numlinea': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['localcommuter.Linea']"}),
            'tipofrec': ('django.db.models.fields.CharField', [], {'max_length': '25'})
        },
        u'localcommuter.parada': {
            'Meta': {'object_name': 'Parada'},
            '_id': ('django.db.models.fields.IntegerField', [], {'primary_key': 'True', 'db_column': "'_id'"}),
            'lat': ('django.db.models.fields.FloatField', [], {}),
            'long': ('django.db.models.fields.FloatField', [], {}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '25'})
        },
        u'localcommuter.rutalinea': {
            'Meta': {'object_name': 'RutaLinea'},
            '_id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'numlinea': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['localcommuter.Linea']"}),
            'numparada': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['localcommuter.Parada']"}),
            'orden': ('django.db.models.fields.IntegerField', [], {}),
            'sentido': ('django.db.models.fields.IntegerField', [], {})
        },
        u'localcommuter.sistematransporte': {
            'Meta': {'object_name': 'SistemaTransporte'},
            '_id': ('django.db.models.fields.IntegerField', [], {'primary_key': 'True', 'db_column': "'_id'"}),
            'descripcion': ('django.db.models.fields.TextField', [], {'max_length': '250', 'blank': 'True'}),
            'nombre': ('django.db.models.fields.CharField', [], {'max_length': '25'})
        }
    }

    complete_apps = ['localcommuter']