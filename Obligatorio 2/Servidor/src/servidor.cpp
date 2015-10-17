// Básicas
#include <cstdlib>
#include <stdlib.h>
#include <vector>
#include <string.h>
#include <iostream>
#include <time.h>
#include <cmath>
#include <unistd.h>

// Sockets
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

// Multi-threading
#include <pthread.h>
#include <thread>
#include <mutex>
#include <condition_variable>

// Debugging
#include <bitset>

using namespace std;

#define MAX_MESSAGE_LENGHT 1023		// (65507 byte - 1 byte RDT header)
#define MAX_NICKNAME_LENGHT 64		// Largo del nickname
#define MAX_PACKET_SIZE 1024		// Tamaño máximo para el payload de un paquete UDP (65,535 − 8 byte UDP header − 20 byte IP header)
#define multicastIP "225.5.4.3"		// IP a la que el servidor envía por multicast
#define multicastPort 6789			// Puerto al que el servidor envía por multicast
#define TIMEOUT_RDT_UNICAST 0.5		// en segundos
#define TIMEOUT_RDT_MULTICAST 0.3	// en segundos

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Buffer %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% //

struct BoundedBuffer {
	char** buffer;
	int capacity;

	int front;
	int rear;
	int count;

	mutex lock;

	condition_variable not_full;
	condition_variable not_empty;

	BoundedBuffer(int capacity) : capacity(capacity), front(0), rear(0), count(0) {
		buffer = new char*[capacity];
	}

	~BoundedBuffer() {
		delete[] buffer;
	}

	void deposit(char* data) {
		unique_lock<mutex> l(lock);

		not_full.wait(l, [this]() {
			return count != capacity; });

		buffer[rear] = data;
		rear = (rear + 1) % capacity;
		++count;

		not_empty.notify_one();
	}

	char* fetch() {
		unique_lock<mutex> l(lock);

		not_empty.wait(l, [this]() {
			return count != 0; });

		char* result = buffer[front];
		front = (front + 1) % capacity;
		--count;

		not_full.notify_one();

		return result;
	}
};

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Variables globales %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% //

struct cliente {
	char nick[MAX_NICKNAME_LENGHT];
	sockaddr_in address;
	time_t last_seen;
	bool puedo_enviar;
};

mutex lista_clientes_mutex;
vector<cliente> lista_clientes;

struct cliente_rdt {
	sockaddr_in address;
	time_t timer;
	int estado_sender; // De 0 a 3
	bool expected_seq_num; // el seq num que espero recibir del cliente
	char* ult_pkt_enviado;
	//BoundedBuffer* buffer_de_salida;
};

mutex lista_clientes_rdt_mutex;
vector<cliente_rdt> lista_clientes_rdt;

struct cliente_multicast {
	sockaddr_in address;
	bool recibido;
};

mutex lista_clientes_multicast_mutex;
vector<cliente_multicast> lista_clientes_multicast;

bool seqNum_multicast = false; // el último número que envié
time_t timer_multicast = 0;
char* ult_pkt_multicast_enviado = new char[MAX_PACKET_SIZE];

// Variables del socket multicast
int sockMulticast;
struct in_addr iaddr;
struct sockaddr_in servMulticAddr, servMulticInterface, multic_cliaddr;
unsigned char ttl = 5;
unsigned char one = 1;

// Variables del socket unicast
int sockUnicast;
struct sockaddr_in servUnicAddr, unic_cliaddr;

//Variables para interface
int cantClientes = 0;
int cantMensajes = 0;
int cantConexiones = 0;
std::chrono::duration<double> wallTime;
chrono::system_clock::time_point actual;

// %%%%%%%%%%%%%%%%%%%% Forward Declarations %%%%%%%%%%%%%%%%%%%%%% //

void rdt_send_unicast(char* msj, const sockaddr_in& cli_addr, bool quitar_cliente);

void udt_send_multicast(char* msj);

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% APLICACIÓN %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% //

void deliver_message(char* msj, const sockaddr_in cli_addr) {
	lock_guard<mutex> lock(lista_clientes_mutex);

	// Imprime en salida standar cada vez que se recibe un mensaje como pide la letra
	cout << "Mensaje recibido desde: " << inet_ntoa(cli_addr.sin_addr) << ":" << ntohs(cli_addr.sin_port) << endl;
	cout << "MSJ> " << msj << endl << endl;

	// Parseo del mensaje recibido
	char * comando = strtok(msj, " ");
	if (strcmp(comando, "LOGIN") == 0) {
		// Obtengo el nick
		char * nick = strtok(NULL, " ");

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
			// El nick ya está en uso
			string resp = "LOGIN_FAILED";
			char *resp_ptr = new char[resp.length() + 1];
			*resp_ptr = 0;
			strcpy(resp_ptr, resp.c_str());
			thread t1(rdt_send_unicast, resp_ptr, cli_addr, true);
			t1.detach();
		} else {
			// El nick está disponible
			string resp = "LOGIN_OK";
			char *resp_ptr = new char[resp.length() + 1];
			*resp_ptr = 0;
			strcpy(resp_ptr, resp.c_str());
			thread t1(rdt_send_unicast, resp_ptr, cli_addr, false);
			t1.detach();

			// Añadir el cliente a la lista
			cliente nuevo_cliente;
			strcpy(nuevo_cliente.nick, nick);
			nuevo_cliente.address = cli_addr;
			nuevo_cliente.last_seen = time(NULL);
			lista_clientes.push_back(nuevo_cliente);

			lista_clientes_multicast_mutex.lock();
			// Lo agrego a la lista multicast
			cliente_multicast nuevo_cliente_m;
			nuevo_cliente_m.address = cli_addr;
			lista_clientes_multicast.push_back(nuevo_cliente_m);
			lista_clientes_multicast_mutex.unlock();

			// Envío aviso por multicast
			string aviso = "> " + string(nick) + " está ahora en línea";
			char *aviso_ptr = new char[aviso.length() + 1];
			*aviso_ptr = 0;
			strcpy(aviso_ptr, aviso.c_str());
			thread t2(udt_send_multicast, aviso_ptr);

			t2.detach();

			cantClientes++;
			cantConexiones++;
		}

	} else if (strcmp(comando, "LOGOUT") == 0) {
		// Envío respuesta al cliente
		string resp = "GOODBYE";
		char *resp_ptr = new char[resp.length() + 1];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());
		thread t1(rdt_send_unicast, resp_ptr, cli_addr, true);
		t1.detach();

		// Quitar al cliente de la lista
		char * remitente;
		bool encontre = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not encontre && it != lista_clientes.end()) {
			if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
				strcpy(remitente, it->nick);
				lista_clientes.erase(it);
				encontre = true;
			} else {
				it++;
			}
		}

		lista_clientes_multicast_mutex.lock();
		// Quitar al cliente de la lista multicast
		bool encontre2 = false;
		vector<cliente_multicast>::iterator it2 = lista_clientes_multicast.begin();
		while (not encontre2 && it2 != lista_clientes_multicast.end()) {
			if (it2->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it2->address.sin_port == cli_addr.sin_port) {
				lista_clientes_multicast.erase(it2);
				encontre2 = true;
			} else {
				it2++;
			}
		}
		lista_clientes_multicast_mutex.unlock();

		if (encontre) {
			// Envío aviso por multicast
			string aviso = "El usuario ";
			aviso += string(remitente) + " se ha desconectado.";
			char *aviso_ptr = new char[aviso.length() + 1];
			*aviso_ptr = 0;
			strcpy(aviso_ptr, aviso.c_str());
			thread t2(udt_send_multicast, aviso_ptr);
			t2.detach();

			cantClientes--;
		}

	} else if (strcmp(comando, "IS_ALIVE") == 0) {
		bool encontre = false;
		vector<cliente>::iterator it = lista_clientes.begin();
		while (not encontre && it != lista_clientes.end()) {
			if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
				//actualizo last seen a ahora
				it->last_seen = time(NULL);
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

		// Envío
		thread t1(rdt_send_unicast, resp_ptr, cli_addr, false);
		t1.detach();
		cantMensajes++;

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
			} else {
				it++;
			}
		}

		char * resp_ptr = new char[MAX_MESSAGE_LENGHT];
		*resp_ptr = 0;

		strcpy(resp_ptr, resp.c_str());

		// Obtengo el texto del mensaje
		char * mensaje = strchr(msj, '\0') + 1;

		// concateno ambos
		strcat(resp_ptr, mensaje);

		// Envío
		thread t1(udt_send_multicast, resp_ptr);
		t1.detach();
		cantMensajes++;

		// Testing solamente(solo cambiar la ip)
		//		sockaddr_in cliente_over_hamachi;
		//		cliente_over_hamachi.sin_family = PF_INET;
		//		cliente_over_hamachi.sin_addr.s_addr = inet_addr("25.105.117.246");
		//		cliente_over_hamachi.sin_port = htons(6789);
		//		char * resp_ptr2 = new char[MAX_MESSAGE_LENGHT];
		//		*resp_ptr2 = 0;
		//		strcpy(resp_ptr2, resp.c_str());
		//		strcat(resp_ptr2, mensaje);
		//		thread t2(rdt_send_unicast, resp_ptr2, cliente_over_hamachi);
		//		t2.detach();

	} else if (strcmp(comando, "PRIVATE_MESSAGE") == 0) {
		// Enviar por unicast el mensaje

		// Creo el cabezal
		string resp = "PRIVATE_MESSAGE ";

		// Obtengo el nick del destinatario
		char * destinatario = strtok(NULL, " ");

		// Obtengo el texto del mensaje
		char * mensaje = strtok(NULL, "");

		// Busco el nick del remitente
		char * remitente;
		bool encontreCliente = false;
		{
			vector<cliente>::iterator it = lista_clientes.begin();
			while (not encontreCliente && it != lista_clientes.end()) {
				if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
					remitente = it->nick;
					encontreCliente = true;
				} else {
					it++;
				}
			}
		}

		// Agrego el remitente (y un espacio) a la respuesta
		resp += string(remitente) + " ";
		char * resp_ptr = new char[MAX_MESSAGE_LENGHT];
		*resp_ptr = 0;
		strcpy(resp_ptr, resp.c_str());

		// Le agrego el mensaje a la respuesta
		strcat(resp_ptr, mensaje);

		// Busco la dirección del destinatario
		sockaddr_in dest_addr;
		bool encontreDestinatario = false;
		{
			vector<cliente>::iterator it = lista_clientes.begin();
			while (not encontreDestinatario && it != lista_clientes.end()) {
				if (strcmp(it->nick, destinatario) == 0) {
					dest_addr = it->address;
					encontreDestinatario = true;
				} else {
					it++;
				}
			}
		}

		if (encontreDestinatario) {
			// Envío
			thread t1(rdt_send_unicast, resp_ptr, dest_addr, false);
			t1.detach();
			cantMensajes++;

		} else { //el destinatario no está conectado
			delete [] resp_ptr;
			string resp2 = "MP_FAILED El usuario " + string(destinatario) + " no se encuentra en línea.";
			char *resp_ptr2 = new char[resp2.length() + 1];
			*resp_ptr2 = 0;
			strcpy(resp_ptr2, resp2.c_str());
			thread t1(rdt_send_unicast, resp_ptr2, cli_addr, false);
			t1.detach();
		}

	}
};

void unicastSocket_setUp() {
	memset(&sockUnicast, 0, sizeof (struct sockaddr_in));
	sockUnicast = socket(AF_INET, SOCK_DGRAM, 0);
	servUnicAddr.sin_family = AF_INET;
	servUnicAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servUnicAddr.sin_port = htons(54321);
	cout << "Servidor escuchando unicast en " << inet_ntoa(servUnicAddr.sin_addr) << ":" << ntohs(servUnicAddr.sin_port) << endl;
	bind(sockUnicast, (struct sockaddr *) &servUnicAddr, sizeof (servUnicAddr));
}

void multicastSocket_setUp() {
	// Creo el socket UDP
	sockMulticast = socket(PF_INET, SOCK_DGRAM, 0);
	if (sockMulticast < 0) perror("Error creating socket");

	memset(&servMulticAddr, 0, sizeof (struct sockaddr_in));
	memset(&servMulticInterface, 0, sizeof (struct sockaddr_in));

	// Set local interface address
	servMulticInterface.sin_family = PF_INET;
	servMulticInterface.sin_port = htons(0);
	servMulticInterface.sin_addr.s_addr = htonl(INADDR_ANY);

	// Bind socket to local interface
	int status = bind(sockMulticast, (struct sockaddr *) &servMulticInterface, (socklen_t)sizeof (servMulticInterface));
	if (status < 0) perror("Error binding socket to interface");

	// Set multicast packet TTL; default TTL is 1
	setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_TTL, &ttl, sizeof (unsigned char));

	// Set destination address
	servMulticAddr.sin_family = PF_INET;
	servMulticAddr.sin_addr.s_addr = inet_addr(multicastIP);
	servMulticAddr.sin_port = htons(multicastPort);
}

void ping_clientes() {
	string alive_str = "ALIVE";
	lock_guard<mutex> lock(lista_clientes_mutex);
	if (not lista_clientes.empty()) {
		for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end(); it++) {
			char * alive_ptr = new char [alive_str.length() + 1];
			strcpy(alive_ptr, alive_str.c_str());
			thread t1(rdt_send_unicast, alive_ptr, it->address, false);
			t1.detach();
		}
	}
}

void ping_clientes_trigger() {
	while (true) {
		ping_clientes();
		usleep(1000000); // 1 sec
	}
}

void update_clientes() {
	lock_guard<mutex> lock(lista_clientes_mutex);
	time_t now = time(NULL);
	bool encontre = false;
	vector<cliente>::iterator it = lista_clientes.begin();
	while (not encontre && it != lista_clientes.end()) {
		//si no lo vi por mas de 5 seg
		if (difftime(now, it->last_seen) > 3) {
			// Quitar al cliente de la lista
			char * remitente = new char[MAX_NICKNAME_LENGHT];
			strcpy(remitente, it->nick);
			encontre = true;

			{
				lista_clientes_rdt_mutex.lock();
				// Quitar al cliente de la lista rdt
				bool encontre2 = false;
				vector<cliente_rdt>::iterator it2 = lista_clientes_rdt.begin();
				while (not encontre2 && it2 != lista_clientes_rdt.end()) {
					if (it2->address.sin_addr.s_addr == it->address.sin_addr.s_addr && it2->address.sin_port == it->address.sin_port) {
						lista_clientes_rdt.erase(it2);
						encontre2 = true;
					} else {
						it2++;
					}
				}
				lista_clientes_rdt_mutex.unlock();
			}

			{
				lista_clientes_multicast_mutex.lock();
				// Quitar al cliente de la lista multicast
				bool encontre2 = false;
				vector<cliente_multicast>::iterator it2 = lista_clientes_multicast.begin();
				while (not encontre2 && it2 != lista_clientes_multicast.end()) {
					if (it2->address.sin_addr.s_addr == it->address.sin_addr.s_addr && it2->address.sin_port == it->address.sin_port) {
						lista_clientes_multicast.erase(it2);
						encontre2 = true;
					} else {
						it2++;
					}
				}
				lista_clientes_multicast_mutex.unlock();
			}

			// Envío aviso por multicast
			string aviso = "El usuario ";
			aviso += string(remitente) + " se ha desconectado por inactividad.";
			char *aviso_ptr = new char[aviso.length() + 1];
			*aviso_ptr = 0;
			strcpy(aviso_ptr, aviso.c_str());
			lista_clientes.erase(it);
			thread t2(udt_send_multicast, aviso_ptr);
			t2.detach();
			delete [] remitente;
			cantClientes--;
		} else {
			it++;
		}
	}
}

void update_clientes_trigger() {
	while (true) {
		usleep(3000000);
		update_clientes();
	}
}

void leer_entrada() {
	while (true) {
		char c = cin.get();
		switch (c) {
			case 'a':
				cout << cantClientes << " clientes en línea." << endl;
				break;
			case 's':
				cout << cantMensajes << " mensajes enviados." << endl;
				break;
			case 'd':
				cout << cantConexiones << " conexiones (total)." << endl;
				break;
			case 'f':
				wallTime = (chrono::system_clock::now() - actual);
				cout << "Up time: " << floor(wallTime.count()) << " segundos" << endl;
				break;
			default:;
		}
	}
}

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% CONFIABILIDAD %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% //

bool is_ACK(char pkt_header) {
	// Devuelve true si el paquete es un acknowledge
	return (pkt_header >> 7) & 1;
}

bool hasSeqNum(char pkt_header, bool expectedSeqNum) {
	// Devuelve true si el numero de secuencia del paquete coincide con expectedSeqNum
	return ((pkt_header & 1) == expectedSeqNum);
}

bool getSeqNum(char pkt_header) {
	return (pkt_header & 1);
}

char* makepkt_unicast(bool is_ACK, bool seqNum, char* msj) {
	if (is_ACK) {
		char* pkt = new char;
		*pkt = seqNum | 128;
		return pkt;
	} else {
		char* pkt = new char[strlen(msj) + 2];
		// Escribo el header
		pkt[0] = seqNum | 2;

		// Escribo el mensaje
		for (int i = 0; i < strlen(msj); i++) {
			pkt[i + 1] = msj[i];
		};

		// Lo termino con un fin de línea
		pkt[strlen(msj) + 1] = 0;
		//delete [] msj;
		return pkt;
	}
}

char* makepkt_multicast(bool seqNum, char* msj) {
	char* pkt = new char[strlen(msj) + 2];
	// Escribo el header
	pkt[0] = seqNum | 2;

	// Escribo el mensaje
	for (int i = 0; i < strlen(msj); i++) {
		pkt[i + 1] = msj[i];
	};

	// Lo termino con un fin de línea
	pkt[strlen(msj) + 1] = 0;
	//delete [] msj;
	return pkt;
}

void udt_send_multicast(char* msj) {
	cout << "udt_send_multicast: " << msj << endl << endl;
	sendto(sockMulticast, msj, strlen(msj), 0, (struct sockaddr *) &servMulticAddr, sizeof (struct sockaddr_in));
	delete [] msj;
}

void udt_send_unicast(char* msj, const sockaddr_in& cli_addr) {
	char* p = &msj[1];
	cout << "udt_send_unicast header: " << bitset<8>(msj[0]) << " y mensaje: " << p << endl;
	cout << "to: " << inet_ntoa(cli_addr.sin_addr) << ":" << ntohs(cli_addr.sin_port) << endl << endl;
	sendto(sockUnicast, msj, strlen(msj), 0, (struct sockaddr *) &cli_addr, sizeof (cli_addr));
	delete [] msj;
}

void rdt_send_multicast(char* msj) {
	char* pkt = makepkt_multicast(seqNum_multicast, msj);
	strcpy(ult_pkt_multicast_enviado, pkt);
	thread t1(udt_send_multicast, pkt);
	t1.detach();
	timer_multicast = time(NULL);
}

void rdt_send_unicast(char* msj, const sockaddr_in & cli_addr, bool quitar_cliente) {
	// Busco el cliente
	lock_guard<mutex> lock(lista_clientes_rdt_mutex);
	bool encontre_cliente = false;
	cliente_rdt* cliente_ptr;
	vector<cliente_rdt>::iterator it = lista_clientes_rdt.begin();
	while (not encontre_cliente && it != lista_clientes_rdt.end()) {
		if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
			cliente_ptr = &(*it);
			encontre_cliente = true;
		} else {
			it++;
		}
	}

	// Armo el paquete
	char* pkt = makepkt_unicast(false, cliente_ptr->estado_sender / 2, msj);

	// Pongo el mensaje en el buffer de salida
	//    cliente_ptr->buffer_de_salida->deposit(pkt);
	if (quitar_cliente) {
		lista_clientes_rdt.erase(it);
	} else {
		// Actualizo el estado del cliente
		cliente_ptr->estado_sender = (cliente_ptr->estado_sender + 1) % 4;
		cliente_ptr->timer = time(NULL);
		strcpy(cliente_ptr->ult_pkt_enviado, pkt);
	}
	// Envío
	thread t1(udt_send_unicast, pkt, cli_addr);
	t1.detach();
}

void rdt_rcv_unicast(char* msj, sockaddr_in cli_addr) {
	lock_guard<mutex> lock(lista_clientes_rdt_mutex);
	char seqNum = msj[0];
	// Busco el cliente
	bool encontre_cliente = false;
	cliente_rdt* cliente_ptr;
	vector<cliente_rdt>::iterator it = lista_clientes_rdt.begin();
	while (not encontre_cliente && it != lista_clientes_rdt.end()) {
		if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
			cliente_ptr = &(*it);
			encontre_cliente = true;
		} else {
			it++;
		}
	}

	if (encontre_cliente) {
		if (is_ACK(msj[0])) {
			cout << "Es ACK" << endl;
			// Es un ACK para procesar desde el sender
			// Me fijo en qué estado estoy para ese cliente y lo actualizo
			if ((hasSeqNum(msj[0], 0) && cliente_ptr->estado_sender == 1)
					|| (hasSeqNum(msj[0], 1) && cliente_ptr->estado_sender == 3)) {
				cliente_ptr->timer = 0;
				cliente_ptr->estado_sender = (cliente_ptr->estado_sender + 1) % 4;
			}
		} else {
			cout << "No es ACK" << endl;
			// Es un mensaje nuevo
			// Me fijo si tiene el número de seq esperado
			if (hasSeqNum(msj[0], cliente_ptr->expected_seq_num)) {
				// Actualizo el estado del cliente
				cliente_ptr->expected_seq_num = !cliente_ptr->expected_seq_num;

				// Lo paso a la aplicación
				thread t1(deliver_message, &msj[1], cli_addr);
				t1.detach();
			}
			// En cualquier caso le contesto con un ACK y el mismo seqNum
			//salvo que haya recibido un ACK
			thread t2(udt_send_unicast, makepkt_unicast(true, getSeqNum(seqNum), NULL), cli_addr);
			t2.detach();
		}
	} else if (!is_ACK(msj[0])) {
		// Cliente nuevo.
		// Añadir el cliente a la lista
		cout << "Nuevo cliente" << endl;
		cliente_rdt nuevo_cliente;
		nuevo_cliente.address = cli_addr;
		nuevo_cliente.estado_sender = 0;
		nuevo_cliente.expected_seq_num = 1;
		nuevo_cliente.ult_pkt_enviado = new char[MAX_PACKET_SIZE];
		nuevo_cliente.timer = 0;
		//nuevo_cliente.buffer_de_salida = new BoundedBuffer(1);
		lista_clientes_rdt.push_back(nuevo_cliente);

		// Paso el mensaje a la aplicación
		thread t1(deliver_message, &msj[1], cli_addr);
		t1.detach();

		// En cualquier caso le contesto con un ACK y el mismo seqNum
		thread t2(udt_send_unicast, makepkt_unicast(true, getSeqNum(seqNum), NULL), cli_addr);
		t2.detach();
	}
}

void udt_rcv_unicast() {
	char buffer[MAX_PACKET_SIZE] = {0};
	// Para guardar la dirección del cliente
	struct sockaddr_in si_cliente;
	int slen = sizeof (si_cliente);

	while (true) {
		recvfrom(sockUnicast, buffer, MAX_PACKET_SIZE, 0, (struct sockaddr *) &si_cliente, (socklen_t *) & slen);
		// Obtengo un puntero al comienzo del mensaje
		char* temp = &buffer[1];
		char* pkt = new char[strlen(temp) + 2];

		// Escribo el mensaje
		for (int i = 0; i < strlen(temp) + 1; i++) {
			pkt[i] = buffer[i];
		};

		// Lo termino con un fin de línea
		pkt[strlen(temp) + 1] = 0;
		cout << "udt_rcv_unicast header: " << bitset<8>(pkt[0]) << endl;
		char* p = &pkt[1];
		cout << "y data: " << p << endl << endl;
		thread t1(rdt_rcv_unicast, pkt, si_cliente);
		t1.detach();
	}
}

void timeout_checker_unicast() {
	time_t now = time(NULL);
	lock_guard<mutex> lock(lista_clientes_rdt_mutex);
	for (vector<cliente_rdt>::iterator it = lista_clientes_rdt.begin(); it != lista_clientes_rdt.end(); it++) {
		if (it->timer != 0 && difftime(now, it->timer) > TIMEOUT_RDT_UNICAST) {
			// Re-envío
			char* pkt = new char[strlen(it->ult_pkt_enviado) + 1];
			strcpy(pkt, it->ult_pkt_enviado);
			thread t1(udt_send_unicast, pkt, it->address);
			t1.join();

			// Seteo el timer
			it->timer = time(NULL);
		}
	}
}

void timeout_checker_unicast_trigger() {
	while (true) {
		usleep(500000); // 500ms
		timeout_checker_unicast();
	}
}

void timeout_checker_multicast() {
	time_t now = time(NULL);
	if (timer_multicast != 0 && difftime(now, timer_multicast) > TIMEOUT_RDT_MULTICAST) {
		// Re-envío
		char* pkt = new char[strlen(ult_pkt_multicast_enviado) + 1];
		strcpy(pkt, ult_pkt_multicast_enviado);
		thread t1(udt_send_multicast, pkt);
		t1.join();

		// Seteo el timer
		timer_multicast = time(NULL);
	}
}

void timeout_checker_multicast_trigger() {
	while (true) {
		usleep(500000); // 500ms
		timeout_checker_multicast();
	}
}

int main(int argc, char** argv) {
	// Creo y configuro los sockets
	unicastSocket_setUp();
	multicastSocket_setUp();

	// Seteo el reloj de wall time
	actual = chrono::system_clock::now();

	// Thread que lee desde cin
	thread t1(leer_entrada);
	t1.detach();

	// El thread que hace ping a los clientes
	thread t2(ping_clientes_trigger);
	t2.detach();

	// El thread que chequea si los clientes respondieron al ping
	thread t3(update_clientes_trigger);
	t3.detach();

	// <! %%%%%%		USE WITH CAUTION	%%%%%% !>
	// El thread que chequea si hay que reenviar los mensajess unicast
	//	thread t4(timeout_checker_unicast_trigger);
	//	t4.detach();

	// El thread que chequea si hay que reenviar los mensajess multicast
	//	thread t5(timeout_checker_multicast_trigger);
	//	t5.detach();

	// Loop escuchando en unicast
	udt_rcv_unicast();

	return 0;
}
