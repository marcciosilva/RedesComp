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

void rdt_send_unicast(char* msj, const sockaddr_in& cli_addr) {
	cout << "rdt_send_unicast message: " << msj << endl;
	sendto(sockUnicast, msj, sizeof (msj), 0, (struct sockaddr *) &cli_addr, sizeof (cli_addr));
	delete [] msj;
}

void rdt_send_multicast(char* msj) {
	cout << "rdt_send_multicast message: " << msj << endl;
	sendto(sockMulticast, msj, strlen(msj), 0, (struct sockaddr *) &servMulticAddr, sizeof (struct sockaddr_in));
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
			thread t1(rdt_send_unicast, resp_ptr, cli_addr);
			t1.detach();
		} else {
			// El nick está disponible. Envío "OK"
			string resp = "OK";
			char *resp_ptr = new char[resp.length() + 1];
			*resp_ptr = 0;
			strcpy(resp_ptr, resp.c_str());
			thread t1(rdt_send_unicast, resp_ptr, cli_addr);
			t1.detach();

			// Añadir el cliente a la lista
			cliente nuevo_cliente;
			strcpy(nuevo_cliente.nick, nick);
			nuevo_cliente.address = cli_addr;
			nuevo_cliente.last_seen = time(NULL);
			lista_clientes.push_back(nuevo_cliente);
		}
	} else if (strcmp(comando, "LOGOUT") == 0) {
		// Envío respuesta al cliente
		string resp = "GOODBYE";
		char *resp_ptr = new char[resp.length() + 1];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());
		thread t1(rdt_send_unicast, resp_ptr, cli_addr);
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

	} else if (strcmp(comando, "GET_CONNECTED") == 0) {
		// Enviar los nombres de los clientes conectados
		string resp = "CONNECTED";
		for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end(); it++) {
			resp = resp + " " + it->nick;
		}
		char *resp_ptr = new char[resp.length() + 1];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());
		thread t1(rdt_send_unicast, resp_ptr, cli_addr);
		t1.detach();

	} else if (strcmp(comando, "MESSAGE") == 0) {
		// Hacer multicast del mensaje

		// Creo el cabezal
		string resp = "RELAYED_MESSAGE ";

		// Busco el nickname del cliente
		bool encontre = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not encontre && it != lista_clientes.end()) {
			if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
				resp = resp + it->nick + " ";
				encontre = true;
			}
			it++;
		}

		char * resp_ptr = new char[MAX_MESSAGE_LENGHT];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());

		// Obtengo el texto del mensaje
		char * mensaje = strchr(msj, '\0') + 1;

		// concateno ambos
		strcat(resp_ptr, mensaje);

		// Envío
		thread t1(rdt_send_multicast, resp_ptr);
		t1.detach();

	} else if (strcmp(comando, "PRIVATE_MESSAGE") == 0) {
		// Enviar por unicast el mensaje

		// Creo el cabezal
		string resp = "PRIVATE_MESSAGE ";
		char * resp_ptr = new char[MAX_MESSAGE_LENGHT];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());
		
		// Obtengo el nick del destinatario
		char * destinatario = strtok(NULL, " ");
		
		// Obtengo el texto del mensaje
		char * mensaje = strchr(msj, '\0') + 1;

		// concateno ambos
		strcat(resp_ptr, mensaje);

		// Busco la dirección del destinatario
		bool encontre = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not encontre && it != lista_clientes.end()) {
			if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
				resp = resp + it->nick + " ";
				encontre = true;
			}
			it++;
		}
		
		// Envío
		//rdt_send(cabezal_ptr, ip, puerto);
	}
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

void multicastSocket_setUp() {
	// Creo el socket UDP
	memset(&servMulticAddr, 0, sizeof (struct sockaddr_in));
	sockMulticast = socket(PF_INET, SOCK_DGRAM, 0);
	if (sockMulticast < 0) perror("Error creating socket");

	servMulticAddr.sin_family = PF_INET;
	servMulticAddr.sin_port = htons(0); // No cambiar! Debe ser el que use el cliente para enviar sus ACK
	servMulticAddr.sin_addr.s_addr = htonl(INADDR_ANY); // bind socket to any interface
	int status = bind(sockMulticast, (struct sockaddr *) &servMulticAddr, (socklen_t)sizeof (servMulticAddr));
	if (status < 0) perror("Error binding socket to interface");

	memset(&iaddr, 0, sizeof (struct in_addr));
	iaddr.s_addr = INADDR_ANY; // use DEFAULT interface

	// Set the outgoing interface to DEFAULT
	setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_IF, &iaddr, sizeof (struct in_addr));

	// Set multicast packet TTL; default TTL is 1
	setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_TTL, &ttl, sizeof (unsigned char));

	// Send multicast traffic to myself too (sirve para el testing si se corren el cliente y el servidor en el mismo host)
	setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_LOOP, &one, sizeof (unsigned char));

	// Set destination address
	servMulticAddr.sin_family = PF_INET;
	servMulticAddr.sin_addr.s_addr = inet_addr("225.5.4.3");
	servMulticAddr.sin_port = htons(6789); // No cambiar! Debe ser el mismo que use el cliente para recibir
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
	multicastSocket_setUp();
	crear_cliente();
	listar_clientes();
	rdt_rcv_unicast();

	return 0;
}
