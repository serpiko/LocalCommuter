#!/bin/env python
# -*- coding: utf-8 -*-

#Modificar el webcrawler para que recorra todas las paradas y de la url+numParada:
#http://www.emtpalma.es/EMTPalma/Front/donde_estoy.es.svr?accion=cargauna&codParada=547
#
#extraiga el patron
#<a style="position.*inline;\".*maps\?ll=(\d{:2}\.\d{:5}),(\d{:2}\.\d{:5})& donde  \1 y \2 son las coordenadas gps de la parada numParada
#
#<a style="position: static; overflow: visible; float: none; display: inline;" target="_blank" href="http://maps.google.com/maps?ll=39.54941,2.729263&

#Version Abril2013
'''No recorremos todas las url sino que directamente en el servlet emtpalma.es/EMTPalma/Front/donde_estoy.es.svr se pueden consultar todas las paradas'''
#Dado que hay que recorrerlo 2 veces ( una para conseguir todas las paradas con el regex re.findall y otra para mapear las paradas con sus líneas ( truncando el fichero por líneas de texto y analizando cada una  con re.match) descargamos el contenido a una copia local
#A su vez conviene modificar el charset del fichero a utf8 para su manipulación así que también por este motivo se descarga el contenido actual del servlet con las paradas.

from unidecode import unidecode
import re
import os, sys
import urllib2
import urllib
from urllib2 import URLError, HTTPError
from BeautifulSoup import BeautifulSoup
import urlparse
import csv
#import codecs
#from django.utils.encoding import smart_str


sitename = "http://www.emtpalma.es/EMTPalma/Front/donde_estoy.es.svr"
dstfile = "htmlparadas-unicode-escaped4.txt"

dicStops = {}

def descargaServlet():
    htmlfile = urllib.urlopen(sitename)
#    page = BeautifulSoup((''.join(htmlfile)))
#    page2 = str(page)
#    texto = page2.decode('unicode-escape')
    texto = htmlfile.read().decode('unicode-escape')
    #escapamos el texto unicode y sanitizamos
    #utexto = unidecode.remove_accents(texto)
    #content = smart_str(utexto)
    content = unidecode(texto)

    fdest = open(dstfile, "wb")
    fdest.write(content)
    fdest.close()


def gestDic(parada, x, y ):
## with open("data.csv", "wb") as f:
      csv.writer(f).writerows((k,) + v for k, v in maxDict.iteritems())
      f = open("relacion_lineas_pdf","rw")
        
    #si ya está esa key crea una derivada linea 1_a

def saveDic():
    with open("diccionario_paradas.txt","wb") as f:
        csv.writer(f).writerows((k,) + v for k, v in dicLineas.iteritems())
    
def main():
    descargaServlet()


    
if __name__ == '__main__':
    main()
