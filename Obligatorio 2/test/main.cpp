// Básicas
#include <cstdlib>
#include <vector>
#include <string.h>
#include <iostream>
#include <time.h>

// Sockets
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

// Multi-threading
#include <pthread.h>
#include <thread>
#include <mutex>

using namespace std;

// Estructura de datos compartida por todos los threads

#define MAX_MESSAGE_LENGHT 1024 // Tamaño del payload
#define MAX_NICKNAME_LENGHT 64	// Largo del nickname
#define MAX_PACKET_SIZE 65536	// Tamaño máximo para un paquete UDP

struct cliente {
	char nick[MAX_NICKNAME_LENGHT];
	sockaddr_in address;
	time_t last_seen;
};

mutex lista_clientes_mutex;
vector<cliente> lista_clientes;

// Variables del socket multicast
int sockMulticast;
struct in_addr iaddr;
struct sockaddr_in servMulticAddr, multic_cliaddr;
unsigned char ttl = 5;
unsigned char one = 1;

// Variables del socket unicast
int sockUnicast;
struct sockaddr_in servUnicAddr, unic_cliaddr;

void rdt_send(char* msj, const sockaddr_in& cli_addr) {
	sendto(sockUnicast, msj, sizeof (msj), 0, (struct sockaddr *) &cli_addr, sizeof (cli_addr));
	delete [] msj;
}

void deliver_message(char* msj, const sockaddr_in& cli_addr) {
	lock_guard<mutex> lock(lista_clientes_mutex); // el lock se libera automáticamente al finalizar el bloque
	char * comando = strtok(msj, " ");

	cout << "Dentro del deliver_message." << endl;
	cout << "Comando: " << comando << endl;
	cout << "IP: " << inet_ntoa(cli_addr.sin_addr) << endl;
	cout << "Puerto: " << ntohs(cli_addr.sin_port) << endl;


	if (strcmp(comando, "LOGIN") == 0) {
		// Obtengo el nick
		char * nick = strtok(NULL, " ");
		cout << "NICK: " << nick << endl;

		// Verifico si ya existe un cliente con ese nick
		bool nick_en_uso = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not nick_en_uso && it != lista_clientes.end()) {
			if (strcmp(it->nick, nick) == 0) {
				nick_en_uso = true;
			}
			it++;
		}

		if (nick_en_uso) {
			// El nick ya está en uso. Envío "NOK"
			string resp = "NOK";
			char *resp_ptr = new char[resp.length() + 1];
			*resp_ptr = 0;
			strcpy(resp_ptr, resp.c_str());
			thread t1(rdt_send, resp_ptr, cli_addr);
			t1.detach();
		} else {
			// El nick está disponible. Envío "OK"
			string resp = "OK";
			char *resp_ptr = new char[resp.length() + 1];
			*resp_ptr = 0;
			strcpy(resp_ptr, resp.c_str());
			thread t1(rdt_send, resp_ptr, cli_addr);
			t1.detach();
		}

		// Añadir el cliente a la lista
		cliente nuevo_cliente;
		strcpy(nuevo_cliente.nick, nick);
		nuevo_cliente.address = cli_addr;
		nuevo_cliente.last_seen = time(NULL);
		lista_clientes.push_back(nuevo_cliente);

	} else if (strcmp(comando, "LOGOUT") == 0) {
		// Envío respuesta al cliente
		string resp = "GOODBYE";
		char *resp_ptr = new char[resp.length() + 1];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());
		thread t1(rdt_send, resp_ptr, cli_addr);
		t1.detach();

		// Quitar al cliente de la lista
		bool encontre = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not encontre && it != lista_clientes.end()) {
			if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
				lista_clientes.erase(it);
				encontre = true;
			}
			it++;
		}

	} //else if (strcmp(commndo, "GET_CONNECTED") == 0) {
	//		// Enviar los nombres de los clientes conectados
	//		string resp = "CONNECTED ";
	//		for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end(); it++) {
	//			resp = resp + it->nick;
	//		}
	//		char * resp_ptr;
	//		strcpy(resp_ptr, resp.c_str());
	//		//rdt_send(resp_ptr, ip, puerto);
	//
	//	} else if (strcmp(comando, "MESSAGE") == 0) {
	//		// Hacer multicast del mensaje
	//
	//		// Creo el cabezal
	//		string cabezal = "RELAYED_MESSAGE ";
	//		char * cabezal_ptr = new char[MAX_MESSAGE_LENGHT];
	//		strcpy(cabezal_ptr, cabezal.c_str());
	//
	//		// Obtengo el mensaje del msj recibido
	//		char * mensaje = strchr(msj, ' ');
	//
	//		// concateno ambos
	//		strcat(cabezal_ptr, mensaje);
	//
	//		// Envío
	//		//rdt_broadcast(cabezal_ptr);
	//		//thread t1(rdt_broadcast, cabezal_ptr);
	//
	//	} else if (strcmp(comando, "PRIVATE_MESSAGE") == 0) {
	//		// Enviar por unicast el mensaje
	//
	//		// Creo el cabezal
	//		string cabezal = "PRIVATE_MESSAGE ";
	//		char * cabezal_ptr = new char[MAX_MESSAGE_LENGHT];
	//		strcpy(cabezal_ptr, cabezal.c_str());
	//
	//		// Obtengo el mensaje del msj recibido
	//		char * mensaje = strchr(msj, ' ');
	//
	//		// concateno ambos
	//		strcat(cabezal_ptr, mensaje);
	//
	//		// Envío
	//		//rdt_send(cabezal_ptr, ip, puerto);
	//	}
};

void rdt_rcv_unicast() {
	char buffer[MAX_PACKET_SIZE];
	// Para guardar la dirección del cliente
	struct sockaddr_in si_cliente;
	int slen = sizeof (si_cliente);
	int i = 1;

	while (true) {
		cout << "Esperando mensaje unicast nro " << i << endl;
		i++;
		recvfrom(sockUnicast, buffer, MAX_PACKET_SIZE, 0, (struct sockaddr *) &si_cliente, (socklen_t *) & slen);
		char* temp = buffer;
		thread t1(deliver_message, temp, si_cliente);
		t1.detach();
	}
}

void unicastSocket_setUp() {
	memset(&sockUnicast, 0, sizeof (struct sockaddr_in));
	sockUnicast = socket(AF_INET, SOCK_DGRAM, 0);
	servUnicAddr.sin_family = AF_INET;
	servUnicAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servUnicAddr.sin_port = htons(54321);
	cout << "Servidor escuchando en unicast, puerto 54321" << endl;
	bind(sockUnicast, (struct sockaddr *) &servUnicAddr, sizeof (servUnicAddr));
}

void crear_cliente() {
	cliente c;
	c.last_seen = time(NULL);
	strcpy(c.nick, "gaston");

	lock_guard<mutex> lock(lista_clientes_mutex);
	lista_clientes.push_back(c);
}

void listar_clientes() {
	lock_guard<mutex> lock(lista_clientes_mutex);

	for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end(); it++) {
		cout << it->nick << endl;
	}
}

int main(int argc, char** argv) {
	unicastSocket_setUp();
	crear_cliente();
	listar_clientes();
	rdt_rcv_unicast();

	return 0;
}

