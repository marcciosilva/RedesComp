#!/usr/bin/python
# -*- coding: UTF-8 -*-
import subprocess
import re
import sys, getopt
from time import sleep

def ayuda():
	texto = ['Este programa toma como parámetros un FQDN y el tipo de RR a consultar', 
		"y retorna una respuesta autoritativa.",
		'',
		"usage:	3.py [options] dominio",
		"options:",
		"	-h --help			Muestra este menú",
		"	-c				Tipo de consulta (valores válidos: A|AAAA|SOA|NS|MX|CNAME|TXT|SRV)",
		"					(ANY por defecto)",
		"dominio:",
		"	Una FQDN bien formada"]
	print '\n'.join(texto)
	return

args = sys.argv[1:]

dominio = ''
RR = ''

try:
	opts, args = getopt.getopt(args,"hc:",["help","RR="])
except getopt.GetoptError:
	ayuda()
	sys.exit(1)
for opt, arg in opts:
	if opt in ('-h','--help'):
		print ayuda()
		sys.exit()
	elif opt in ('-c'):
		RR = arg
		
if not RR:
	RR = 'ANY'
else:
	if not re.search('ANY|A|AAAA|SOA|NS|MX|CNAME|TXT|SRV', RR):
		sys.exit("El parametro ingresado no es correcto.\nVuelva a intentarlo.")

if args:
	dominio = args[0]
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
for s in servidoresAuth:
	authorityCmd = "@" + s
	dig = subprocess.Popen(["dig", authorityCmd, dominio, RR, "+noall", "+answer", "+comments"], stdout=subprocess.PIPE)
	sleep(1.0)
	if dig.poll() is None:
		dig.terminate()
	else:
		encontre = True
		break

if not encontre:
	print "No se obtuvo respuesta de los servidores autoritativos."
	sys.exit(1)
	
salida = dig.communicate()[0]

notADomain = re.compile('NXDOMAIN')
noError = re.compile('NOERROR')
answer = re.compile('ANSWER SECTION')

if notADomain.search(salida):
		print "El dominio ingresado no existe."
		print "status:NXDOMAIN"
		sys.exit(1)
elif noError.search(salida):
	if RR == 'ANY':
		rr = ['A','AAAA','NS','SOA','MX','TXT']
	else:
		rr = [RR]
	if answer.search(salida):
		for x in rr:
			if x in ('A','AAAA'):
				r = '\s+' + x + '\s+([\d|\.|\:|\w]*)'
			elif x in ('NS','SOA'):
				r = '\s+' + x + '\s+([\w|\.]*)'
			elif x in ('MX'):
				r = '\s+' + x + '\s+\d+\s+([\w|\.]*)'
			else:
				r = '\s+' + x + '\s+(".*")'
			rc = re.compile(r)
			if rc.search(salida):
				for i in rc.findall(salida):
					if i != '':
						print i
						
			#soa = re.compile('\s+SOA\s+([\w|\.]*)')
			#elif soa.search(salida):
			#	for i in soa.findall(salida):
			#		print i
	else:
		print "No se obtuvo respuesta para la consulta realizada."
		print "Se consultó el servidor autoritativo " + s
		sys.exit(1)
else:
	print "Error desconocido.\nVuelva a intentarlo con otro dominio."
	sys.exit(1)