__author__ = 'marccio'

import pexpect
import re

#recibo direccion del host
host = raw_input('Enter host address: ')
print
print '-----------------------------------'
print
#pexcpect.spawn se usa para invocar un comando en la linea de comandos del sistema
child = pexpect.spawn('traceroute ' + host)
ttl = '' #aca se guarda un ttl actual
dictTtlTiempo = {} #aca guardo correspondencia ttl - tiempo promedio
avgTiemposRouters = [] #se guardan los tiempos promedios, para poder recorrerlos independientemente del diccionario
while 1:
        #mientras hayan lineas para leer originadas del comando, se examinan
        line = child.readline()
        if not line:
            break
        #si es la linea inicial, no intento registrar ttl, porque no hay
        esLineaInicial = re.match(r'traceroute', line)
        if not (esLineaInicial):
            searchObject = re.match('\s?(\d*)\s+', line)
            if searchObject:
                ttl = searchObject.groups()[0]
        #separo la linea actual en strings, de acuerdo a la cantidad de routers
        reg = re.compile('(\d\s+)*([\w\d\.\-]+)\s+(\([\d\.]+\))\s+([\d\.]+\sms|\*)\s*([\d\.]+\sms|\*)?\s*([\d\.]+\sms|\*)?')
        #se genera una lista de strings, donde cada string tiene la salida para un router
        routers = reg.findall(line)
        #si se matcheo algo
        if routers:
            cantRouters = len(routers) #cantidad de routers que recibieron al menos una sonda con el ttl actual
            routersStringsIndividuales = [] #aca voy a juntar las strings, que quedaron segmentadas en el matcheo
            for i in range(cantRouters):
                routersStringsIndividuales.append(' '.join(routers[i]))
            #ahora tengo una lista de strings, donde cada string tiene la informacion de retorno de cada router
            #en el comando traceroute - queda aplicarle otra expresion regular a cada una para sacar los valores
            #de rtt
            tiempoPromedioRouters = [] #para cada router, voy a guardar su tiempo promedo; este arreglo va a tener
            #los tiempos de respuesta para un router en cada momento
            for i in range(cantRouters):
                reg = re.compile('\s\d*\.\d*\s') #matcheo tiempos de respuesta
                tiempos = reg.findall(routersStringsIndividuales[i])
                avg = 0.0
                #calculo promedio para el router actual
                for i in range(len(tiempos)):
                    tiempos[i].replace(' ', '')
                    avg += float(tiempos[i])
                avg = avg / len(tiempos)
                #lo agrego a los tiempos promedio
                tiempoPromedioRouters.append(avg)
            #el tiempo que voy a registrar para este ttl va a ser el maximo de los tiempos de respuesta en promedio
            #de todos los routers que respondieron
            #lo agrego al diccionario asociado a su ttl
            dictTtlTiempo[ttl] = max(tiempoPromedioRouters)
            #tambien lo agrego a mi arreglo de tiempos promedio
            avgTiemposRouters.append(max(tiempoPromedioRouters))
            print line
            print "(El mayor tiempo del router con ttl = "+ttl+" es: "+str(max(tiempoPromedioRouters))+" ms)"
            print
tiempoMaximoHop = 0
idHop = ''
#determino el hop que supuso el mayor aumento de tiempo de respuesta del traceroute
for i in range(len(avgTiemposRouters)):
    if (i != 0):
        tiempoHop = abs(avgTiemposRouters[i] - avgTiemposRouters[i-1])
        if (abs(avgTiemposRouters[i] - avgTiemposRouters[i-1]) > tiempoMaximoHop):
            tiempoMaximoHop = tiempoHop
            for id, tiempo in dictTtlTiempo.iteritems():
                if tiempo == avgTiemposRouters[i]:
                    idHop = id
print "El tiempo maximo entre hops es: "+str(tiempoMaximoHop)+" ms, correspondiente al hop que termina en el ttl "+idHop+" ."
print "Ese hop, por lo tanto, es el mayor responsable por el crecimiento en el tiempo de respuesta del comando"