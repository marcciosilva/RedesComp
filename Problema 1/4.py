__author__ = 'marccio'
import pexpect
import re

print 'Este programa compara los tiempos de respuesta'
print 'entre ejecuciones de ping con y sin la flag -n'
host = raw_input('Ingrese la direccion de un host: ')
packets = int(raw_input('Ingrese la cantidad de packets a enviar en cada ping: '))
i = int(raw_input('Ingrese la cantidad de veces a evaluar cada ping: '))
print
print '-----------------------------------'
print

#creo listas para ir almacenando avg rtt en cada iteracion
flagList = []
noFlagList = []

for x in range(i):
    child = pexpect.spawn('ping -c ' + str(packets) + ' ' + host)
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
                #sabiendo cual es el de rtt average, lo asigno a una variable
                rttMax1 = float(numbers[1])
                noFlagList.append(rttMax1)
                print
            print line
print
print '-----------------------------------'
print

for x in range(i):
    child = pexpect.spawn('ping -n -c ' + str(packets) + ' ' + host)
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
                #sabiendo cual es el de rtt average, lo asigno a una variable
                rttMax1 = float(numbers[1])
                flagList.append(rttMax1)
                print
            print line

print
print '-----------------------------------'
print

#calculo promedio de rtt promedio
averageFlagList = 0
for x in range(len(flagList)):
    averageFlagList += flagList[x]
averageFlagList /= len(flagList)
averageNoFlagList = 0
for x in range(len(noFlagList)):
    averageNoFlagList += noFlagList[x]
averageNoFlagList /= len(noFlagList)

minAvgTime = min(averageFlagList, averageNoFlagList)
print
print '-----------------------------------'
print
if averageFlagList > averageNoFlagList:
    print str(averageNoFlagList) + " ms es el menor tiempo promedio (promedio de tiempos promedio), resultando ping sin -n el mas rapido"
elif averageFlagList < averageNoFlagList:
    print str(averageFlagList) + " ms es el menor tiempo promedio (promedio de tiempos promedio), resultando ping con -n el mas rapido"
else:
    print "Ambos comandos resultaron igual de rapidos en promedo"