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
	texto = ["Este programa toma como parámetro una IP o FQDN e identifica los",
		"ISPs y/o carriers internacionales utilizados.",
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
			
if not argumento:
	ayuda()
	sys.exit(1)
		
traceroute = subprocess.Popen(["./traceroute", argumento], stdout=subprocess.PIPE)
traceroute.wait()
salida = traceroute.communicate()[0]
lineas = salida.splitlines()
lineas = lineas[1:]

cabezal = '\s?\d+\s\s'
ip = '(\(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\))'
fqdn = '([\w\.\-]+)'
tiempo = '\d+\.\d{3}\sms'

hit = fqdn + '\s' + ip + '\s\s' + tiempo +'\s?'
hitx2 = hit + '\s' + tiempo + '\s?'
hitx3 = hitx2 + '\s' + tiempo
miss = '\*\s?'


h3 = re.compile(hitx3)
h2h = re.compile(hitx2+hit)
hh2 = re.compile(hit+hitx2)
hhh = re.compile(hit+hit+hit)
hhm = re.compile(hit+hit+miss)
h2m = re.compile(hitx2+miss)
hmh = re.compile(hit+miss+hit)
mhh = re.compile(miss+hit+hit)
mh2 = re.compile(miss+hitx2)
mmh = re.compile(miss+miss+hit)
mhm = re.compile(miss+hit+miss)
hmm = re.compile(hit+miss+miss)
mmm = re.compile(miss+miss+miss)

isp = []
ip = []

#lineas = [' 1  192.168.1.1 (192.168.1.1)  0.374 ms  0.471 ms  0.572 ms',
#' 2  tia2bras3.antel.net.uy (200.40.78.198)  45.025 ms  47.806 ms  48.771 ms',
#' 3  ibb2agu3-3-0-0.antel.net.uy (200.40.78.48)  49.762 ms ibb2agu4-3-0-0.antel.net.uy (200.40.78.50)  51.718 ms  53.687 ms',
#' 4  cbb2agu1-be150.antel.net.uy (200.40.78.1)  55.710 ms cbb2tia1-be200.antel.net.uy (200.40.78.5)  60.652 ms  61.828 ms',
#' 5  ibe2agu1-be150.antel.net.uy (200.40.78.2)  63.757 ms  63.853 ms  67.893 ms',
#' 6  ibr2nap4-0-1-4-0.antel.net.uy (200.40.16.38)  220.746 ms  201.956 ms ibr2nap3-0-2-1-0.antel.net.uy (200.40.16.174)  205.654 ms',
#' 7  xe-9-3-2.edge4.Miami1.Level3.net (4.59.90.45)  217.381 ms te0-0-0-8.ccr21.mia03.atlas.cogentco.com (38.104.95.245)  193.560 ms *',
#' 8  ae-2-70.edge3.Washington4.Level3.net (4.69.149.82)  224.645 ms be2054.ccr21.mia01.atlas.cogentco.com (154.54.80.41)  189.580 ms ae-2-70.edge3.Washington4.Level3.net (4.69.149.82)  228.430 ms',
#' 9  be2122.ccr41.atl01.atlas.cogentco.com (154.54.24.193)  215.406 ms ae-1-60.edge3.Washington4.Level3.net (4.69.149.18)  238.524 ms be2122.ccr41.atl01.atlas.cogentco.com (154.54.24.193)  226.434 ms',
#'10  * * ae-46.r05.asbnva02.us.bb.gin.ntt.net (129.250.5.191)  224.465 ms',
#'11  * * *']

for i in lineas:
	if h3.search(i):
		linea = h3.findall(i)

	elif h2h.search(i):
		linea = h2h.findall(i)

	elif hh2.search(i):
		linea = hh2.findall(i)

	elif hhh.search(i):
		linea = hhh.findall(i)
		
	elif hhm.search(i):
		linea = hhm.findall(i)
		
	elif h2m.search(i):
		linea = h2m.findall(i)

	elif hmh.search(i):
		linea = hmh.findall(i)

	elif mhh.search(i):
		linea = mhh.findall(i)

	elif mh2.search(i):
		linea = mh2.findall(i)

	elif mmh.search(i):
		linea = mmh.findall(i)

	elif mhm.search(i):
		linea = mhm.findall(i)

	elif hmm.search(i):
		linea = hmm.findall(i)

	elif mmm.search(i):
		linea = mmm.findall(i)
	isp.append((linea[0][0]))
	ip.append(linea[0][1])

#filtro las entradas sin datos y los () de la lista de ips
ip2 = []
isp2 = []
reg_ip = re.compile('\((\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})\)')
for i in range(0,len(ip)):
	if isp[i] not in ('*'):
		match_obj = reg_ip.match(ip[i])
		ip2.append(match_obj.group(1))
		isp2.append(isp[i])

ip = ip2
isp = isp2
merge = zip(isp,ip)
#merge = [(isp[1],ip[1])]

for i,j in merge:
	#print "curl","ipinfo.io/" + j 
	curl = subprocess.Popen(["curl","ipinfo.io/" + j + "/country"], stdout=subprocess.PIPE,stderr=subprocess.PIPE)
	curl.wait()
	salida = curl.communicate()[0]
	salida = salida.splitlines()
	if salida[0] != 'UY':
		print i + " pertenece a ---> " + salida[0]
		









