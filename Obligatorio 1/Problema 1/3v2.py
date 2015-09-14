#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess
import re
import sys, getopt
import time

def ayuda():
	texto = ["Este programa toma como parámetro dos hosts, que se pretenden monitorizar,", 
		 "y un umbral de tiempo de respuesta, y muestra en pantalla un mensaje solo cuando",
		 "exista algún problema. El problema podrá ser o que no se alcanza al destino o",
		 "que el tiempo de respuesta supera un umbral especificado.",
		 '',
		 "usage:	3.py [options] host1 host2",
		 "options:",
		 "	-h --help			Muestra este menú",
		 "	-t umbral			en milisegundos ej: 0.500",
		 "						(500 ms por defecto)",
		 "host1 host2:",
		 "	FQDN o IP."]
	print '\n'.join(texto)
	return

def ping(host):
	ping = subprocess.Popen(["ping", host, "-c", "3"], stdout=subprocess.PIPE)
	salida = ping.communicate()[0]

	success = re.compile('statistics')
	if not success.search(salida):
		print "Alguno de los hosts no pudo ser alcanzado"
		sys.exit(1)
	else:
		ultLinea = re.findall('=\s(.*)\sms',salida)
		tiempos = re.findall('(\d+\.\d+)/?',ultLinea[0])
	return tiempos[2]

host1 = ''
host2 = ''
umbral = ''

args = sys.argv[1:]
try:
	opts, args = getopt.getopt(args,"ht:",["help"])
except getopt.GetoptError:
	ayuda()
	sys.exit(1)
	
for opt, arg in opts:
	if opt in ('-h','--help'):
		print ayuda()
		sys.exit()
	elif opt in ('-t'):
		umbral = arg

if not umbral:
	umbral = '500.0'
else:
	if not re.search('\d+(\.\d+)?',umbral):
		print "El tiempo ingresado no es correcto\nParámetro ingresado: " + umbral
		sys.exit(1)

if args:
	host1 = args[0]
	host2 = args[1]
else:
	ayuda()
	sys.exit(1)

while(True):
	avg1 = ping(host1)
	avg2 = ping(host2)
	if float(avg1) >= float(umbral):
		print "El host " + host1 + " ha superado el umbral de " + umbral + " ms"
	elif float(avg2) >= float(umbral):
		print "El host " + host2 + " ha superado el umbral de " + umbral + " ms"
	#time.sleep(1)