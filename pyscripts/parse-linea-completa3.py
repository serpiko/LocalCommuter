#!/usr/bin/env python 
# -*- coding: utf-8 -*-
"""Captura las lineas con su información completa, frecuencia y orden de paradas"""
#Versión con crawler web completo de todas las líneas

import re
import urllib2
from HTMLParser import HTMLParser
from urllib2 import URLError, HTTPError


###
url = "http://www.emtpalma.es/EMTPalma/Front/lineas.es.svr?accion=entrada&cod_linea="
###
totallineas = 50
arrayQueries = []
def parseLinea(content, lineactual):

    #instancia HTMLParser() para escapar los caracteres unicode como apostrofes
    h = HTMLParser()

    #patrones de la información que queremos extraer en formato regex
    nombrelinea = re.compile( r"^\W*<span\sstyle=\"color:.*\">\r$ ")
    tipodia = re.compile( r"(Laborables|Festivos)" )
    freq = re.compile( r"Frecuencia media:.(\d+)" )
    ida = re.compile( r"div class=\"ida" )
    vuelta = re.compile( r"div class=\"vuelta" )
    parada = re.compile( r"<a class=\"nombre\".*id=\"parada(\d*)_(\d*)" )

    i=0
    counttiempo = 0
    listaparadas = []

    tipofrec = ""
    ultimosentido = 0
    nlinea = "" #nombre de linea para insert
    for linea in content:
#        print i
        i+=1
        match = re.search( r"^\W*<span\sstyle=\"color:.*\">\r$" , linea )
        if match:

            nlinea = h.unescape(content[i])
            nlinea = nlinea.strip()
            print "INSERT INTO \"LINEAS\" VALUES(" + str(lineactual) + ", \"" + str(nlinea) + "\");"
            query = "INSERT INTO \"LINEAS\" VALUES(" + str(lineactual) + ", \"" + str(nlinea) + "\");"
            arrayQueries.append(query)

        
        #comprobamos cada regex
        #DONE: se ha comprobado que la información estática de la línea: nombre, frecuencia media está al 
        #DONE: al principio del fichero, para evitar operaciones de comparación conviene comparar tipo y freq
        #DONE: al principio y cuando se hayan capturado 2 tiempos empezar a comprobar las paradas
        #tiempo sin optimizar: real   0m0.084s
        #tiempo después: real 0m0.080s
        esnombrelinea = re.search( nombrelinea, linea )
        estipo = re.search( tipodia, linea ) 
        esfreq = re.search( freq, linea )
        esida = re.search( ida, linea ) 
        esvuelta = re.search( vuelta, linea ) 
        esparada = re.search( parada, linea )
        if esnombrelinea:
            print content[i]
        elif estipo:
            tipofrec = str(estipo.group(1))
            print tipofrec
#           print str(counttiempo)
        elif esfreq:
            counttiempo+=1
            print tipofrec
            print esfreq.group(1)
            #simulamos insert de linea con: numlinea, nombre temporal, freq laboral, freq festivo
            print "insert into \"LINEA-TIPOFREC\" VALUES(" + str(lineactual) + ", \"" + tipofrec + "\", "+ str(esfreq.group(1))  + ");"
            query = "insert into \"LINEA-TIPOFREC\" VALUES(" + str(lineactual) + ", \"" + tipofrec + "\", "+ str(esfreq.group(1))  + ");\n"
            arrayQueries.append(query)

        #ahora buscamos el orden de paradas
        elif esida:
            #print "ida"
            ultimosentido = 0
        elif esvuelta:
             #print "vuelta"
            ultimosentido = 1
        elif esparada:
            print "parada " + esparada.group(1) + " " + esparada.group(2)
            #simulamos la insert de RUTA-LINEA: numlinea, numparada, orden, sentido
            print "insert into \"RUTA-LINEA\" VALUES(" + str(lineactual) + "," + esparada.group(2) + ", " + esparada.group(1) + ", " + str(ultimosentido) + ");"
            query = "insert into \"RUTA-LINEA\" VALUES(" + str(lineactual) + "," + esparada.group(2) + ", " + esparada.group(1) + ", " + str(ultimosentido) + ");\n"
            arrayQueries.append(query)
        else:
            pass

def initLinea(numlinea):
    url2 = url + str(numlinea)
    try:
        handler = urllib2.urlopen(url2)
        content = handler.readlines()
        handler.close()
    except URLError as e:
        print e.reason
    return content

def saveList():
    ftemp = open("queries-linea-completa.txt", "wb")
    for item in arrayQueries:
        ftemp.write(str(item))
    ftemp.close()


def main():

    lineactual = 0
    #f = open("l3.txt", "rb")
    while (lineactual < totallineas):
    #for numlinea in range(1, totallineas):
        lineactual += 1
        try:
            content = initLinea(lineactual)
            #nomfile = "lineas/linea" + str(lineactual) + "txt"
            #f = open(nomfile, "rb")
            #f.write(content)
            #f.close()
        except:
            continue
        #content = f.readlines()
        #f.close()
        parseLinea(content, lineactual)
        saveList()


if __name__ == "__main__":
    main()
