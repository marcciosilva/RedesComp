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
#include <mutex>

using namespace std;

/* Cliente:
 * 1 thread para escribir/leer en multicast
 * 1 thread para escribir/leer en unicast
 
 * Servidor:
 * 1 thread para recibir y responder en unicast
 * 1 thread para enviar en multicast
 * 1 thread para los comandos
 */

/* Comandos de cliente al servidor
	LOGIN <usuario><CR>
	LOGOUT<CR>
	GET_CONNECTED<CR>
	MESSAGE <msg><CR>
	PRIVATE_MESSAGE <receptor> <msg><CR>

 * Comandos del servidor al cliente:
	RELAYED_MESSAGE <emisor> <msg><CR>
	PRIVATE_MESSAGE <emisor> <msg><CR>
	CONNECTED <usr1>[|<usr2>...]<CR>
	GOODBYE<CR>
 */

// Estructura de datos compartida por todos los threads

#define MAX_MESSAGE_LENGHT 1024 // Tamaño del payload
#define MAX_NICKNAME_LENGHT 64	// Largo del nickname
#define MAX_PACKET_SIZE 65536	// Tamaño máximo para un paquete UDP

struct cliente {
	char nick[MAX_NICKNAME_LENGHT];
	in_addr ip;
	in_port_t puerto;
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

void multicastSocket_setUp(){
	// Creo el socket UDP
	memset(&servMulticAddr, 0, sizeof (struct sockaddr_in));
	sockMulticast = socket(PF_INET, SOCK_DGRAM, 0);
	if (sockMulticast < 0) perror("Error creating socket");

	servMulticAddr.sin_family = PF_INET;
	servMulticAddr.sin_port = htons(6790); // No cambiar! Debe ser el que use el cliente para enviar sus ACK
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

void unicastSocket_setUp(){
	sockUnicast = socket(AF_INET, SOCK_DGRAM, 0);
	servUnicAddr.sin_family = AF_INET;
	servUnicAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servUnicAddr.sin_port = htons(54321);
	bind(sockUnicast, (struct sockaddr *) &servUnicAddr, sizeof (servUnicAddr));
}

void rdt_send(char* msj, in_addr ip, in_port_t puerto) {
	sendto(sockUnicast, msj, sizeof (msj), 0, (struct sockaddr *) &unic_cliaddr, sizeof (unic_cliaddr));
}

void rdt_broadcast(char* msj) {
	sendto(sockMulticast, msj, strlen(msj), 0, (struct sockaddr *) &servMulticAddr, sizeof (struct sockaddr_in));
}

void deliver_message(char* msj, in_addr ip, in_port_t puerto) {
	lock_guard<mutex> lock(lista_clientes_mutex); // el lock se libera automáticamente al finalizar el bloque
	char * commando = strtok(msj, " ");

	if (strcmp(commando, "LOGIN") == 0) {
		// Obtengo el nick
		char * nick = strtok(NULL, " ");

		// Añadir el cliente a la lista
		cliente nuevo_cliente;
		strcpy(nuevo_cliente.nick, nick);
		nuevo_cliente.ip = ip;
		nuevo_cliente.puerto = puerto;
		nuevo_cliente.last_seen = time(NULL);
		lista_clientes.push_back(nuevo_cliente);

	} else if (strcmp(commando, "LOGOUT") == 0) {
		// Envío respuesta al cliente
		string resp = "GOODBYE";
		char * resp_ptr;
		strcpy(resp_ptr, resp.c_str());
		rdt_send(resp_ptr, ip, puerto);

		// Quitar al cliente de la lista
		bool encontre = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not encontre && it != lista_clientes.end()) {
			if (it->ip.s_addr == ip.s_addr && it->puerto == puerto) {
				lista_clientes.erase(it);
				encontre = true;
			}
		}

	} else if (strcmp(commando, "GET_CONNECTED") == 0) {
		// Enviar los nombres de los clientes conectados
		string resp = "CONNECTED ";
		for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end(); it++) {
			resp = resp + it->nick;
		}
		char * resp_ptr;
		strcpy(resp_ptr, resp.c_str());
		rdt_send(resp_ptr, ip, puerto);

	} else if (strcmp(commando, "MESSAGE") == 0) {
		// Hacer multicast del mensaje
		
		// Creo el cabezal
		string cabezal = "RELAYED_MESSAGE ";
		char * cabezal_ptr = new char[MAX_MESSAGE_LENGHT];
		strcpy(cabezal_ptr, cabezal.c_str());
		
		// Obtengo el mensaje del msj recibido
		char * mensaje = strchr(msj, ' ');
		
		// concateno ambos
		strcat(cabezal_ptr, mensaje);

		// Envío
		rdt_broadcast(cabezal_ptr);
		//thread t1(rdt_broadcast, cabezal_ptr);
		
	} else if (strcmp(commando, "PRIVATE_MESSAGE") == 0) {
		// Enviar por unicast el mensaje
		
		// Creo el cabezal
		string cabezal = "PRIVATE_MESSAGE ";
		char * cabezal_ptr = new char[MAX_MESSAGE_LENGHT];
		strcpy(cabezal_ptr, cabezal.c_str());
		
		// Obtengo el mensaje del msj recibido
		char * mensaje = strchr(msj, ' ');
		
		// concateno ambos
		strcat(cabezal_ptr, mensaje);

		// Envío
		rdt_send(cabezal_ptr, ip, puerto);
	}
};

int main(int argc, char** argv) {
	unicastSocket_setUp();
	multicastSocket_setUp();

	return 0;
}
