#!/usr/bin/env python

#Script para recorrer ficheros de datos csv con paradas y lineas por parada y volcarlos en formato insert para la db sqlite
import re
from csv import reader


def recreaInserts():
    file = open("stops-dict3.csv","rb")
    content = file.readlines()
    file.close()

    file2 = open('stops-inserts.txt', 'wb')
    regex = r"(?<![\w\d'\)]),"
    i = 0
    for line in content:
        fila = re.sub(r'["\[\]]', "", line)
        #fila = reader(fila)
        i += 1
        #print fila + " " + str(i)
        fila = re.sub(regex,"", fila)
        #print "2print"+fila
        row = fila.split(',')
        #si row = '\n' no imprimir
        if (row[0] <> '\n' ):
            #file2.write( "insert into parada values ( " + row[0] + ", " + row[1] + ", " + row[2] + ", " + row[3] + ");\n")
            file2.write(  row[0] + "|" + row[1] + "|" + row[2] + "|" + row[3])
        #elif IndexError:
        else:
            pass
    file2.close()

if __name__ == "__main__":
    recreaInserts()
