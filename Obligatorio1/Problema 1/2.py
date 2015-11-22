__author__ = 'marccio'
import pexpect
import re

host1 = raw_input('Enter host 1 address: ')
host2 = raw_input('Enter host 2 address: ')
rttMax1 = 0
rttMax2 = 0
print
print '-----------------------------------'
print
#pexcpect.spawn se usa para invocar un comando en la linea de comandos del sistema
child = pexpect.spawn('ping -c 1 ' + host1)
while 1:
        #mientras haya lineas para leer originadas del comando, se examinan
        line = child.readline()
        if not line:
            break
        searchObj = re.search(r'rtt min(.*)', line)
        #en seachObj.string tengo el string que busco
        if searchObj:
            #aca busco todos los valores numericos del string
            numbers = re.findall(r'\d*\.\d*', searchObj.string)
            print "min = "+numbers[0]
            print "avg = "+numbers[1]
            #sabiendo cual es el de rtt maximo, lo asigno a una variable
            rttMax1 = float(numbers[1])
            print "max = "+numbers[2]
            print "mdev = "+numbers[3]
            print
        print line
print
print '-----------------------------------'
print
child = pexpect.spawn('ping -c 1 ' + host2)
while 1:
        line = child.readline()
        if not line:
            break
        searchObj = re.search(r'rtt min(.*)', line)
        #en seachObj.string tengo el string que busco
        if searchObj:
            #aca busco todos los valores numericos del string
            numbers = re.findall(r'\d*\.\d*', searchObj.string)
            print "min = "+numbers[0]
            print "avg = "+numbers[1]
            #sabiendo cual es el de rtt maximo, lo asigno a una variable
            rttMax2 = float(numbers[1])
            print "max = "+numbers[2]
            print "mdev = "+numbers[3]
            print
        print line
minRtt = min(rttMax1, rttMax2)
print
print '-----------------------------------'
print
if minRtt == 0:
    print 'Hubo algun error y no se registraron rtts'
elif minRtt == rttMax1:
    print str(minRtt) + " ms is the lowest average round-trip time, corresponding to host " + host1
else:
    print str(minRtt) + " ms is the lowest average round-trip time, corresponding to host " + host2