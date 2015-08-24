#!/usr/bin/python
import subprocess
import re

dominio = raw_input('Ingrese un nombre de dominio: ')
RR = raw_input('Tipo de consulta (valores validos: ANY/A/AAAA/SOA/NS/MX/CNAME/TXT/SRV): ')

if not re.search('ANY|A|AAAA|SOA|NS|MX|CNAME|TXT|SRV', RR):
	print "\nEl parametro ingresado no es correcto.\nVuelva a intentarlo."
	exit()

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
dig = subprocess.Popen(["dig", authorityCmd, dominio, RR, "+noall", "+answer"], stdout=subprocess.PIPE)
salida = dig.communicate()[0]

#temp = re.findall('\d+\s+([\w|\.]*)', salida)
print "\nLa respuesta obtenida del servidor autoritativo " + authority + "es la siguiente:\n"
print salida