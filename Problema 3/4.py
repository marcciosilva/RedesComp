#!/usr/bin/python
import subprocess
import re

#dominio = raw_input('Ingrese un nombre de dominio: ')
dominio = "google.com"
#IP = raw_input('Ingrese una direccion IP: ')

dig = subprocess.Popen(["dig", dominio, "NS", "+noadditional", "+nostats"], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

notADomain = re.compile('NXDOMAIN')
noError = re.compile('NOERROR')

if noError.search(salida):
	ns = re.compile('\s+NS\s+([\w|\.]*)')

	if ns.search(salida):
		for i in ns.findall(salida):
			if i != '':
				authority = i
				break
	else:
		print "Error desconocido."

elif notADomain.search(salida):
	print "\nEl dominio ingresado no existe."
	print "status:NXDOMAIN"
	
else:
	print "\nError desconocido.\nVuelva a intentarlo con otro dominio."
	
authorityCmd = "@" + authority
dig = subprocess.Popen(["dig", authorityCmd, dominio, "MX", "+noall", "+additional"], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

print "\nSe consulto al servidor autoritativo: " + authority +"\n"
ips = re.findall('(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})', salida)

#encontre = re.compile(IP)
for i in ips:
	print i