__author__ = 'marccio'
import pexpect
import re

host1 = raw_input('Enter host 1 address: ')
host2 = raw_input('Enter host 2 address: ')
print
print '-----------------------------------'
print
error = False
errors = [r'.* unreachable', r'.* failed', r'.* unknown', r'.* fragmentation',
          r'.* isolated', r'.* prohibited']
child1 = pexpect.spawn('ping -c 10 ' + host1)
child2 = pexpect.spawn('ping -c 10 ' + host2)
turnoChild1 = True
while 1:
    if (turnoChild1):
        line = child1.readline()
        for x in range(len(errors)):
            searchObj = re.search(errors[x], line)
            if searchObj:
                error = True
                break
        if not error:
            #chequeo si hubo perdida de paquetes
            searchObj = re.search(r'(\d*)% packet loss.*', line)
            if searchObj and int(searchObj.group(1)) > 0:
                print "Hubo perdida de paquetes al " + str(searchObj.group(1)) + '%'
        turnoChild1 = False
    else:
        line = child2.readline()
        for x in range(len(errors)):
            searchObj = re.search(errors[x], line)
            if searchObj:
                error = True
                break
        if not error:
            #chequeo si hubo perdida de paquetes
            searchObj = re.search(r'(\d*)% packet loss.*', line)
            if searchObj and int(searchObj.group(1)) > 0:
                print "Hubo perdida de paquetes al " + str(searchObj.group(1)) + '%'
        turnoChild1 = True
    if not line:
        break
    if error:
        print line
        error = False