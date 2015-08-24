#!/usr/bin/python
import subprocess
import re

dominio = raw_input('Ingrese un nombre de dominio: ')

dig = subprocess.Popen(["dig", dominio, "+noadditional", "+nostats"], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

notADomain = re.compile('NXDOMAIN')
noError = re.compile('NOERROR')

if noError.search(salida):
	print "\n-----------------------------------------------------------------"
	print "\nLos servidores de nombre autoritativos para " + dominio + " son:\n"

	soa = re.compile('\s+SOA\s+([\w|\.]*)')
	ns = re.compile('\s+NS\s+([\w|\.]*)')
	if soa.search(salida):
		for i in soa.findall(salida):
			print i
	elif ns.search(salida):
		for i in ns.findall(salida):
			print i
	else:
		print "Error desconocido."

elif notADomain.search(salida):
	print "\nEl dominio ingresado no existe."
	print "status:NXDOMAIN"
	
else:
	print "\nError desconocido.\nVuelva a intentarlo con otro dominio."