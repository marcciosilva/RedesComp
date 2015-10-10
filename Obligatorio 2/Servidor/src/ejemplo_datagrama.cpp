#include <cstdlib>
#include <cstdint>
#include <string>
#include <iostream>
#include <stdio.h>
#include <string.h>
#include <bitset>

using namespace std;

struct datagrama{
	unsigned char header; // 1 bit para indicar ACK y 7 bits (0 la 127) para los números de seq
	char data[5];
};

int main(int argc, char** argv) {
	// Como armar un paquete
	char mensaje[5] = "HOLA";
	
	unsigned char header = 0; // pongo todos los bits en 0
	header = 55 | 128; // 128 si es un ACK o nada si no es, 55 es el número de secuencia

	datagrama mi_datagrama;
	mi_datagrama.header = header;
	strcpy(mi_datagrama.data, mensaje);
			
	cout << bitset<8>(header) << endl;
	cout << mi_datagrama.data << endl;
	
	// Como leer un paquete
	// simulo lo que voy a recibir del socket, que es un char []
	char buffer[6];
	memcpy(&buffer, &mi_datagrama, sizeof(mi_datagrama));
	
	bool isACK = buffer[0] & 128;
	int seq_num = buffer[0] & 127;
	char msj [5];
	memcpy(&msj, &buffer[1], 5);
	
	cout << isACK << endl;
	cout << seq_num << endl;
	cout << msj << endl;

	char str[] = "This is a sample string";
	string mesj = "Hola soy pepito";
	char * mesj_arr = new char[64];
	strcpy(mesj_arr, mesj.c_str());
	string cab = "RELAYED_MESSAGE ";
	char * cab_arr = new char[64];
	strcpy(cab_arr, cab.c_str());
	char* out_msj = new char[64];
	strcat(out_msj, cab_arr);
	strcat(out_msj, mesj_arr);
	
	cout << endl << out_msj;
	return 0;
}
