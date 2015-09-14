#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess
import re
import sys, getopt
import readline
import math

def ayuda():
	texto = ["Este programa toma como parámetro dos hosts, que se pretenden monitorizar,", 
		 "y retorna el que presente mejores tiempoe de respuesta.",
		 '',
		 "usage:	2.py [options] host1 host2",
		 "options:",
		 "	-h --help			Muestra este menú",
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
args = sys.argv[1:]

host1 = ''
host2 = ''

try:
	opts, args = getopt.getopt(args,"h",["help"])
except getopt.GetoptError:
	ayuda()
	sys.exit(1)
	
for opt, arg in opts:
	if opt in ('-h','--help'):
		print ayuda()
		sys.exit()
		
if args:
	host1 = args[0]
	host2 = args[1]
else:
	ayuda()
	sys.exit(1)

avg1 = ping(host1)
avg2 = ping(host2)

if float(avg1) <= float(avg2):
	print host1
else:
	print host2
