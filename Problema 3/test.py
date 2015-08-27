#!/usr/bin/python
import subprocess
import re
import sys, getopt

args = sys.argv[1:]
dominio = ''

try:
	opts, args = getopt.getopt(args,"h",["help"])
except getopt.GetoptError:
	print '2.py <nombre_de_dominio>'
	sys.exit(2)
for opt, arg in opts:
	if opt in ('-h','--help'):
		print '2.py <nombre_de_dominio>'
		sys.exit()
		
if args:
	dominio = args[0]
	print 'El dominio ingresado fue: ', dominio
sys.exit()

try:
	opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
except getopt.GetoptError:
	print 'test.py -i <inputfile> -o <outputfile>'
	sys.exit(2)
for opt, arg in opts:
	if opt == '-h':
		print 'test.py -i <inputfile> -o <outputfile>'
		sys.exit()
	elif opt in ("-i", "--ifile"):
		inputfile = arg
	elif opt in ("-o", "--ofile"):
		outputfile = arg
print 'Input file is "', inputfile
print 'Output file is "', outputfile