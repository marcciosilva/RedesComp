#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess
import re
import sys, getopt
import math

def trunc(f):
	prec = 3
	return math.floor(f * (10 ** prec)) / (10 ** prec)

def ayuda():
	texto = ["Este programa toma como parámetro una IP o FQDN e identifica los
		ISPs y/o carriers internacionales utilizados.",
		 '',
		 "usage:	3.py [options] IP|FQDN",
		 "options:",
		 "	-h --help			Muestra este menú",
		 "",
		 "IP:",
		 "	-i <direccion_ip>",
		 "	Ej:		-i 123.22.21.20",
		 "",
		 "FQDN:",
		 "-f <fqdn>",
		 "	Ej:		-f server1.domain.com"]
	print '\n'.join(texto)
	return

args = sys.argv[1:]

argumento = ''

try:
	opts, args = getopt.getopt(args,"hi:f:",["help"])
except getopt.GetoptError:
	ayuda()
	sys.exit(1)
for opt, arg in opts:
	if opt in ('-h','--help'):
		print ayuda()
		sys.exit()
	elif opt in ('-i'):
		if arg:
			if not re.search('(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})', arg):
				sys.exit("El parametro ingresado no es una IP válida.\nVuelva a intentarlo.")
			argumento = arg
		else:
			ayuda()
			sys.exit(1)
	elif opt in ('-f'):
		if arg:
			fqdn = re.compile(r'^([0-9a-z][-\w]*[0-9a-z]\.)+[a-z0-9\-]{2,15}$')
			if not fqdn.match(arg):
				sys.exit("El parametro ingresado no es una FQDN válida.\nVuelva a intentarlo.")
			argumento = arg
		else:
			ayuda()
			sys.exit(1)
		
traceroute = subprocess.Popen(["./traceroute", argumento], stdout=subprocess.PIPE)
traceroute.wait()
salida = traceroute.communicate()[0]
lineas = salida.splitlines()
lineas = lineas[1:]

cabezal = '\s?\d+\s\s'
ip = '\(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\)'
fqdn = '([\w\.\-]+)'
tiempo = '(\d+\.\d{3})\sms'

hit = fqdn + '\s' + ip + '\s\s' + tiempo +'\s?'
hitx2 = hit + '\s' + tiempo + '\s?'
hitx3 = hitx2 + '\s' + tiempo
miss = '(\*)\s?'


h3 = re.compile(hitx3)
h2h = re.compile(hitx2+hit)
hh2 = re.compile(hit+hitx2)
hhh = re.compile(hit+hit+hit)
hhm = re.compile(hitx2+miss)
h2m = re.compile(hitx2+miss)
hmh = re.compile(hit+miss+hit)
mhh = re.compile(miss+hit+hit)
mh2 = re.compile(miss+hitx2)
mmh = re.compile(miss+miss+hit)
mhm = re.compile(miss+hit+miss)
hmm = re.compile(hit+miss+miss)
mmm = re.compile(miss+miss+miss)

a = []
b = []
c = []
isp = []

for i in lineas:
	if h3.search(i):
		linea = h3.findall(i)
		isp.append((linea[0][0]))
		a.append(linea[0][1])
		b.append(linea[0][2])
		c.append(linea[0][3])
		
	elif h2h.search(i):
		linea = h2h.findall(i)
		isp.append((linea[0][0],linea[0][3]))
		a.append(linea[0][1])
		b.append(linea[0][2])
		c.append(linea[0][4])
		
	elif hh2.search(i):
		linea = hh2.findall(i)
		isp.append((linea[0][0],linea[0][2]))
		a.append(linea[0][1])
		b.append(linea[0][3])
		c.append(linea[0][4])
		
	elif hhh.search(i):
		linea = hhh.findall(i)
		isp.append((linea[0][0],linea[0][2],linea[0][4]))
		a.append(linea[0][1])
		b.append(linea[0][3])
		c.append(linea[0][5])
		
	elif hhm.search(i):
		linea = hhm.findall(i)
		isp.append((linea[0][0],linea[0][3]))
		a.append(linea[0][1])
		b.append(linea[0][2])
		c.append(linea[0][3])

	elif h2m.search(i):
		linea = h2m.findall(i)
		isp.append((linea[0][0]))
		a.append(linea[0][1])
		b.append(linea[0][2])
		c.append(linea[0][3])
		
	elif hmh.search(i):
		linea = hmh.findall(i)
		isp.append((linea[0][0],linea[0][3]))
		a.append(linea[0][1])
		b.append(linea[0][2])
		c.append(linea[0][4])
		
	elif mhh.search(i):
		linea = mhh.findall(i)
		isp.append((linea[0][1],linea[0][3]))
		a.append(linea[0][0])
		b.append(linea[0][2])
		c.append(linea[0][4])
		
	elif mh2.search(i):
		linea = mh2.findall(i)
		isp.append((linea[0][1]))
		a.append(linea[0][0])
		b.append(linea[0][2])
		c.append(linea[0][3])
		
	elif mmh.search(i):
		linea = mmh.findall(i)
		isp.append((linea[0][2]))
		a.append(linea[0][0])
		b.append(linea[0][1])
		c.append(linea[0][3])
		
	elif mhm.search(i):
		linea = mhm.findall(i)
		isp.append((linea[0][1]))
		a.append(linea[0][0])
		b.append(linea[0][2])
		c.append(linea[0][3])
		
	elif hmm.search(i):
		linea = hmm.findall(i)
		isp.append((linea[0][0]))
		a.append(linea[0][1])
		b.append(linea[0][2])
		c.append(linea[0][3])
		
	elif mmm.search(i):
		linea = mmm.findall(i)
		isp.append('*')
		a.append(linea[0][0])
		b.append(linea[0][1])
		c.append(linea[0][2])

#hay que trabajar con el arreglo isp[] que ya tiene todos los servidores guardados
#habría que ver cómo se filtran los internacionales