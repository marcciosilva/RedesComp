#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess
import re
import sys, getopt
from time import sleep
testing = 0

def ayuda():
	texto = ['Este programa toma como parámetros un nombre de dominio y una dirección IP.', 
		"Retorna con exit code (0) si esa IP es pertenece a un servidor de correo del dominio ingresado.",
		'',
		"usage:	4.py [options] IP dominio",
		"options:",
		"	-h --help			Muestra este menú",
		"IP:",
		"	Ej: 123.22.21.0",
		"dominio:",
		"	Una FQDN bien formada"]
	print '\n'.join(texto)
	return

args = sys.argv[1:]

dominio = ''
IP = ''


if testing:
	dominio = "google.com"
	IP = "74.125.141.26"
else:
	try:
		opts, args = getopt.getopt(args,"h",["help"])
	except getopt.GetoptError:
		ayuda()
		sys.exit(2)
	for opt, arg in opts:
		if opt in ('-h','--help'):
			print ayuda()
			sys.exit()
	if len(args) >= 2:
		IP = args[0]
		dominio = args[1]
	else:
		ayuda()
		sys.exit()

dig = subprocess.Popen(["./2.py", dominio], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

if dig.returncode:
	print salida
	sys.exit(1)

servidoresAuth = salida.splitlines()
servidoresAuth = servidoresAuth[2:]

encontre = False
ips = []
for s in servidoresAuth:
	authorityCmd = "@" + s
	dig = subprocess.Popen(["dig", authorityCmd, dominio, "MX", "+noall", "+additional"], stdout=subprocess.PIPE)
	sleep(0.5)
	if dig.poll() is None:
		dig.terminate()
	else:
		salida = dig.communicate()[0]
		ipsAux = re.findall('(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})', salida)
		for i in ipsAux:
			if not i in ips:
				ips = ips + [i]

if testing:
	for i in ips:
		print i

if IP in ips:
	print "0"
	sys.exit()
else:
	print "-1"
	sys.exit(-1)