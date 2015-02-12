#!/bin/env python
# -*- coding: utf-8 -*-
import re
import pdfminer
import os, sys
import urllib2
import urllib
from urllib2 import URLError, HTTPError
import urlparse
from BeautifulSoup import BeautifulSoup
import csv
import string
from pdf_to_text2 import convert_pdf

from pdfminer.pdfinterp import PDFResourceManager, process_pdf
from pdfminer.converter import TextConverter
from pdfminer.layout import LAParams
from cStringIO import StringIO

'''prueba para descargar todos los pdf de una url, parsearselos a pdfminer para 
que los convierta a txt y finalmente extraer su información formateada en base a regex usando el módulo re de python'''

urlOrigen ='http://www.emtpalma.es/EMTPalma/Front/lineas.es.svr?accion=entrada&cod_linea=' 
urlAbs = 'http://www.emtpalma.es/EMTPalma/Front/'
#pdfregex = '<a.*href=.imgdb    ' 
dicLineas = {}

#definimos la url
#dado que el formato es http://www.emtpalma.es/EMTPalma/Front/lineas.es.svr?accion=entrada&cod_linea=1
#da la opción a recorrer en un bucle de 1 a 50 pasado por parámetro el cod_linea a la web en cuestión
#se define un rango 
totalLineas = 50

def parseLinea(numLinea):
#    url = urlparse.urlparse(urlOrigen+"linea") 

    url = urlOrigen+str(numLinea)
    print url
    #page = urllib2.urlopen(url).read()
    #req = urllib2.Request(urlOrigen+"1")
    text = ''
    try:
        handler = urllib2.urlopen(url)
        text = handler.read()
        handler.close()
    except URLError as e:
        print e.reason
    soup = BeautifulSoup(text)
    current_link = ''
    for link in soup.findAll('a', href=True):
       # link['href'] = urlparse.urljoin(url, tag['href'])
        current_link = link.get('href')
        if current_link.endswith('pdf'):
            print 'pdf de la linea %s: %s%s' % (numLinea, urlAbs, current_link)
            #print "url absoluta %s" % (url)
            #print "current link %s" %(current_link)
            url=urlAbs+current_link
            ruta = descargaPdf(url)
            if (ruta != ""):
                print convert_pdf(ruta)
            else:
                print "nada que convertir"
            print "gestDic %d %s" % (numLinea, current_link)
            #gestDic(numLinea,current_link,0)
            dicLineas[current_link] = numLinea
            



def gestDic(linea, link,indice):
    #insertamos entrada en diccionario dicLineas
    #for i in string.lowercase:
    if dicLineas.has_key(linea):
        print "existe"
        #añadimos otra entrada de diccionario con el key linea cambiado
        indice+=1
        nomlinea=str(linea)
        nomlinea+="_"
        nomlinea+=indice
        print "existe",nomlinea
    else:
        print "no existe"
        nomlinea=str(linea)+str(indice)
        print nomlinea

    dicLineas[nomlinea] = link
    print "contenido dicLinea para key %s con enlace %s e indice %d" % (nomlinea, link, indice)
    print dicLineas[nomlinea],"\n"
        
    
#    with open("data.csv", "wb") as f:
#        csv.writer(f).writerows((k,) + v for k, v in maxDict.iteritems())
#      f = open("relacion_lineas_pdf","rw")
        
    #si ya está esa key crea una derivada linea 1_a

def saveDic():
    with open("diccionario_pdfs.txt","wb") as f:
        csv.writer(f).writerows((k,v)  for k, v in dicLineas.iteritems())

def convert_pdf(path):

    rsrcmgr = PDFResourceManager()
    retstr = StringIO()
    codec = 'utf-8'
    laparams = LAParams()
    device = TextConverter(rsrcmgr, retstr, codec=codec, laparams=laparams)

    fp = file(path, 'rb')
    process_pdf(rsrcmgr, device, fp)
    fp.close()
    device.close()

    str = retstr.getvalue()
    retstr.close()
    return str


def descargaPdf(url):
    directorio = './pdfs/'
    print "descarga de pdf %s" % (url)
    # descarga de fichero
    #se descompone la url con 
    partes = url.split('/')
    last = len(partes)
    print partes[last-1]
    filename = partes[last-1]
    #print "nombre %s" % filename
    #print type(filename)
    #filename2 = "%s%s" % (directorio,filename)
    filename2 = directorio+filename
    #directorio+=filename
    #print "%s%s" % (directorio,partes[last-1])
    #print directorio

    print filename2
    if (urllib.urlretrieve(url,filename2)):
        convert_pdf(directorio+filename)
        return filename2
    else:
        return ""

    
    
def main():
    for i in range(1,totalLineas):
    #llamada a la funcion para parsear la web y descargar sus pdf
    #TODO: añadir control de error, la función falla si la línea no existe    
        parseLinea(i)
        saveDic()

    
if __name__ == '__main__':
    main()
