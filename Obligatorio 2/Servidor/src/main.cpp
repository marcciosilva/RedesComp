// Básicas
#include <cstdlib>
#include <stdlib.h>
#include <vector>
#include <string.h>
#include <iostream>
#include <time.h>
#include <unistd.h>

// Sockets
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

// Multi-threading
#include <pthread.h>
#include <thread>
#include <mutex>
#include <cmath>

using namespace std;

#define MAX_MESSAGE_LENGHT 65506	// (65507 byte - 1 byte RDT header)
#define MAX_NICKNAME_LENGHT 64		// Largo del nickname
#define MAX_PACKET_SIZE 65507		// Tamaño máximo para el payload de un paquete UDP (65,535 − 8 byte UDP header − 20 byte IP header)
#define multicastIP "225.5.4.3"		// IP a la que el servidor envía por multicast
#define multicastPort 6789			// Puerto al que el servidor envía por multicast

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

//arreglo de booleanos (del tamaño de la cantidad de clientes) para
//determinar quien contesta a los ALIVE
//vector<bool> lista_clientes_alive;

void rdt_send_unicast(char* msj, const sockaddr_in& cli_addr) {
    cout << "rdt_send_unicast message: " << msj << " to: " << inet_ntoa(cli_addr.sin_addr) << ":" << ntohs(cli_addr.sin_port) << endl;
    sendto(sockUnicast, msj, strlen(msj), 0, (struct sockaddr *) &cli_addr, sizeof (cli_addr));
    delete [] msj;
}

void rdt_send_multicast(char* msj) {
    cout << "rdt_send_multicast message: " << msj << endl;
    sendto(sockMulticast, msj, strlen(msj), 0, (struct sockaddr *) &servMulticAddr, sizeof (struct sockaddr_in));
    delete [] msj;
}

void deliver_message(char* msj, const sockaddr_in cli_addr) {
    lock_guard<mutex> lock(lista_clientes_mutex); // el lock se libera automáticamente al finalizar el bloque

    // Imprime en salida standar cada vez que se recibe un mensaje como pide la letra
    cout << "Mensaje recibido desde: " << inet_ntoa(cli_addr.sin_addr) << ":" << ntohs(cli_addr.sin_port) << endl;
    cout << "> " << msj << endl;

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
            // El nick ya está en uso. Envío "NOK"
            string resp = "LOGIN_FAILED";
            char *resp_ptr = new char[resp.length() + 1];
            *resp_ptr = 0;
            strcpy(resp_ptr, resp.c_str());
            thread t1(rdt_send_unicast, resp_ptr, cli_addr);
            t1.detach();
        } else {
            // El nick está disponible. Envío "OK"
            string resp = "LOGIN_OK";
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

            // Envío aviso por multicast
            string aviso = "> " + string(nick) + " está ahora en línea";
            char *aviso_ptr = new char[aviso.length() + 1];
            *aviso_ptr = 0;
            strcpy(aviso_ptr, aviso.c_str());
            thread t2(rdt_send_multicast, aviso_ptr);
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
        thread t1(rdt_send_unicast, resp_ptr, cli_addr);
        t1.detach();

        // Quitar al cliente de la lista
        char * remitente;
        bool encontre = false;
        vector<cliente>::iterator it = lista_clientes.begin();
        while (not encontre && it != lista_clientes.end()) {
            if (it->address.sin_addr.s_addr == cli_addr.sin_addr.s_addr && it->address.sin_port == cli_addr.sin_port) {
                remitente = it->nick;
                lista_clientes.erase(it);
                encontre = true;
            }
            it++;
        }

        // Envío aviso por multicast
        string aviso = "El usuario ";
        aviso += string(remitente) + " se ha desconectado.";
        char *aviso_ptr = new char[aviso.length() + 1];
        *aviso_ptr = 0;
        strcpy(aviso_ptr, aviso.c_str());
        thread t2(rdt_send_multicast, aviso_ptr);
        t2.detach();

        cantClientes--;

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
        thread t1(rdt_send_unicast, resp_ptr, cli_addr);
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
        cantMensajes++;

        // Testing solamente (solo cambiar la ip)
        //		sockaddr_in cliente_over_hamachi;
        //		cliente_over_hamachi.sin_family = PF_INET;
        //		cliente_over_hamachi.sin_addr.s_addr = inet_addr("25.0.32.206");
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
                }
                it++;
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
                }
                it++;
            }
        }

        if (encontreDestinatario) {
            // Envío
            thread t1(rdt_send_unicast, resp_ptr, dest_addr);
            t1.detach();
            cantMensajes++;

        } else { //el destinatario no está conectado
            string resp = "MP_FAILED El usuario " + string(destinatario) + " no se encuentra en línea.";
            char *resp_ptr = new char[resp.length() + 1];
            *resp_ptr = 0;
            strcpy(resp_ptr, resp.c_str());
            thread t1(rdt_send_unicast, resp_ptr, cli_addr);
            t1.detach();
        }


    }
};

void rdt_rcv_unicast() {
    char buffer[MAX_PACKET_SIZE];
    // Para guardar la dirección del cliente
    struct sockaddr_in si_cliente;
    int slen = sizeof (si_cliente);

    while (true) {
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

// Testing solamente

void crear_cliente() {
    cliente c;
    c.last_seen = time(NULL);
    strcpy(c.nick, "gaston");
    memset(&c.address, 0, sizeof (struct sockaddr_in));
    //c.address.sin_family = PF_INET;
    //c.address.sin_addr.s_addr = inet_addr("127.0.0.1");
    //c.address.sin_port = htons(multicastPort);

    // Lo agrego a la lista de clientes
    lock_guard<mutex> lock(lista_clientes_mutex);
    lista_clientes.push_back(c);
}

vector<sockaddr_in>* client_addresses() {
    lock_guard<mutex> lock(lista_clientes_mutex);
    vector<sockaddr_in>* lista_direcciones_ptr = new vector<sockaddr_in>();
    for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end(); it++) {
        lista_direcciones_ptr->push_back(it->address);
    }
    return lista_direcciones_ptr;
}

void ping_clientes() {
    string alive_str = "ALIVE";

    while (true) {
        vector<sockaddr_in>* lista_clientes = client_addresses();
        if (not lista_clientes->empty()) {
            for (vector<sockaddr_in>::iterator it = lista_clientes->begin(); it != lista_clientes->end(); it++) {
                char * alive_ptr = new char [alive_str.length() + 1];
                strcpy(alive_ptr, alive_str.c_str());
                rdt_send_unicast(alive_ptr, *it);
            }
        }
        usleep(1000000); // 1 sec
    }
}

void update_clientes_check() {
    lock_guard<mutex> lock(lista_clientes_mutex);
    time_t now = time(NULL);
    for (vector<cliente>::iterator it = lista_clientes.begin(); it != lista_clientes.end();) {
        //si no lo vi por mas de 5 seg
        if (difftime(now, it->last_seen) > 5) {
            cout << "Algún cliente no contestó el alive en tiempo";
            // Quitar al cliente de la lista
            char * remitente;
            remitente = it->nick;
            //                lock_guard<mutex> lock(lista_clientes_mutex);
            it = lista_clientes.erase(it);

            // Envío aviso por multicast
            string aviso = "El usuario ";
            aviso += string(remitente) + " se ha desconectado.";
            char *aviso_ptr = new char[aviso.length() + 1];
            *aviso_ptr = 0;
            strcpy(aviso_ptr, aviso.c_str());
            thread t2(rdt_send_multicast, aviso_ptr);
            t2.detach();

            cantClientes--;
        } else {
            it++;
        }
    }
}

void update_clientes() {
    while (true) {
        usleep(3000000);
        update_clientes_check();
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

int main(int argc, char** argv) {
    // Creo y configuro los sockets
    unicastSocket_setUp();
    multicastSocket_setUp();

    // Inicio el clock para wallTime
    actual = chrono::system_clock::now();

    //crear_cliente();

    // El thread que hace ping a los clientes
    thread t1(ping_clientes);
    t1.detach();

    // Thread que lee desde cin
    thread t2(leer_entrada);
    t2.detach();

    //thread que cada tanto (más tiempo que el intervalo entre ALIVE's)
    //chequea qué clientes están y qué clientes no están
    thread t3(update_clientes);
    t3.detach();

    // Loop escuchando en unicast
    rdt_rcv_unicast();

    return 0;
}
