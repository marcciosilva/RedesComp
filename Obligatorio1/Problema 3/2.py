#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess
import re
import sys, getopt

def ayuda():
	texto = ["Este programa toma como parámetro un nombre de dominio,", 
		 "y retorna el o los servidores de nombre autoritativos para ese dominio.",
		 '',
		 "usage:	2.py [options] dominio",
		 "options:",
		 "	-h --help			Muestra este menú",
		 "dominio:",
		 "	Una FQDN bien formada"]
	print '\n'.join(texto)
	return

args = sys.argv[1:]

dominio = ''

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
	dominio = args[0]
else:
	ayuda()
	sys.exit(1)

dig = subprocess.Popen(["dig", dominio, "NS", "+noall", "+answer", "+comments"], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

notADomain = re.compile('NXDOMAIN')
noError = re.compile('NOERROR')
answer = re.compile('ANSWER SECTION')

if notADomain.search(salida):
		print "El dominio ingresado no existe."
		print "status:NXDOMAIN"
		sys.exit(1)
elif noError.search(salida):
	if answer.search(salida):
		print "Los servidores de nombre autoritativos para " + dominio + " son:\n"
		ns = re.compile('\s+NS\s+([\w|\.]*)')
		if ns.search(salida):
			for i in ns.findall(salida):
				if i != '':
					print i
					
		#soa = re.compile('\s+SOA\s+([\w|\.]*)')
		#elif soa.search(salida):
		#	for i in soa.findall(salida):
		#		print i
	else:
		#print "No se obtuvo respuesta.\nVuelva a intentarlo con otro dominio."
		sys.exit("No se pudo encontrar un servidor autoritativo.")
else:
	print "Error desconocido.\nVuelva a intentarlo con otro dominio."
	sys.exit(1)