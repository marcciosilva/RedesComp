#!/usr/bin/python
import subprocess
import re

testing = 0
if testing:
	dominio = "google.com"
	IP = "74.125.141.26"
else:
	dominio = raw_input('Ingrese un nombre de dominio: ')
	IP = raw_input('Ingrese una direccion IP: ')

dig = subprocess.Popen(["dig", dominio, "NS", "+noadditional", "+nostats"], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

notADomain = re.compile('NXDOMAIN')
noError = re.compile('NOERROR')

if noError.search(salida):
	ns = re.compile('\s+NS\s+([\w|\.]*)')

	if ns.search(salida):
		authorities = []
		for i in ns.findall(salida):
			if i != '':
				authorities = authorities + [i]
	else:
		print "Error desconocido."

elif notADomain.search(salida):
	print "\nEl dominio ingresado no existe."
	print "status:NXDOMAIN"
	
else:
	print "\nError desconocido.\nVuelva a intentarlo con otro dominio."
	
ips = []
for i in authorities:
	authorityCmd = "@" + i
	dig = subprocess.Popen(["dig", authorityCmd, dominio, "MX", "+noall", "+additional"], stdout=subprocess.PIPE)
	salida = dig.communicate()[0]

	ipsAux = re.findall('(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})', salida)
	for i in ipsAux:
		if not i in ips:
			ips = ips + [i]
for i in ips:
	print i

if IP in ips:
	print "HURRAY"
else:
	print "noooRRAY"
