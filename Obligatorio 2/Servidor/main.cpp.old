//============================================================================
// Name        : Servidor.cpp
// Author      :
// Version     :
// Copyright   : Your copyright notice
// Description : Servidor chat Redes 2015
//============================================================================

// STL

#include <set>

// Expresiones regulares

#include <regex>
#include <sys/socket.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>	/* needed for memset */
#include <netinet/in.h>	/* needed for sockaddr_in */
#include <netdb.h>
#include <arpa/inet.h>
#include <thread>
#define PORT 54321
#define BUFSIZE 1024
#include <sys/types.h>
#include "Cliente.h"

using namespace std;

//struct in_addr localInterface;

struct sockaddr_in groupSock;
int sd;
char databuf[1024] = "Multicast test message lol!";
int datalen = sizeof (databuf);

void task1(string msg) {
    cout << "task1 says: " << msg;
}

int main(int argc, char **argv) {
    struct sockaddr_in myaddr; //our address
    struct sockaddr_in remaddr; //remote address
    socklen_t addrlen = sizeof (remaddr); //length of addresses
    int recvlen; // # bytes received
    int fd; // our socket
    int msgcnt = 0; // count # of messages we received
    char buf[BUFSIZE]; // receive buffer

    set<Cliente*> * clientes = new set<Cliente*>; //acá almaceno la información de cada cliente nuevo

    //    create a UDP socket

    //AF_INET indica que uso una familia de direcciones IP
    //SOCK_DGRAM el tipo de servicio que ofrece el socket; es para UDP
    //el 0 indica que no hay modificaciones adicionales sobre el protocolo
    if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("cannot create socket\n");
        return 0;
    }

    // bind the socket to any valid IP address and a specific port

    memset((char *) &myaddr, 0, sizeof (myaddr));
    myaddr.sin_family = AF_INET;
    myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    myaddr.sin_port = htons(PORT);

    if (bind(fd, (struct sockaddr *) &myaddr, sizeof (myaddr)) < 0) {
        perror("bind failed");
        return 0;
    }
    // loop recibiendo segmentos, evaluando qué thread crear por caso
    while (true) {
        printf("Esperando en puerto %d\n", PORT, "...");
        recvlen = recvfrom(fd, buf, BUFSIZE, 0, (struct sockaddr *) &remaddr,
                &addrlen);
        if (recvlen > 0) {
            buf[recvlen] = 0;
            printf("Mensaje recibido: \"%s\" (%d bytes)\n", buf, recvlen);
        } else
            printf("El mensaje estaba vacío\n");



        sprintf(buf, "ack %d", msgcnt++);
        //hchequeo si es get_connected
        regex regexp_connected("GET\\_CONNECTED\\n");
        string str_connected(buf);
        smatch result_connected;
        regex_search(str_connected, result_connected, regexp_connected);
        if (result_connected[1].str().length() > 0) {
            //estoy en el caso de login, y tengo que agregar al cliente a mi hash
            cout << result_connected[1].str() << endl;
            printf("sending response \"%s\"\n", buf);
            //envío respuesta
            if (sendto(fd, buf, strlen(buf), 0, (struct sockaddr *) &remaddr, addrlen) < 0) {
                perror("sendto");
            }
        } else {
            //chequeo si es login
            regex regexp_login("LOGIN\\s(.*)\\n"); //\\s+\\<(.*)\\>\\<CR>$");
            string str_login(buf);
            smatch result_login;
            regex_search(str_login, result_login, regexp_login);
            if (result_login[1].str().length() > 0) {
                //estoy en el caso de login, y tengo que agregar al cliente a mi hash
                cout << result_login[1].str() << endl;
                printf("sending response \"%s\"\n", buf);
                //envío respuesta
                if (sendto(fd, buf, strlen(buf), 0, (struct sockaddr *) &remaddr, addrlen) < 0) {
                    perror("sendto");
                }
            }
        }
        /* Send Multicast Datagram code example. */

        // Constructs the new thread and runs it. Does not block execution.
        thread t1(task1, "Hello");

        //Makes the main thread wait for the new thread to finish execution, therefore blocks its own execution.
        t1.join();


        return 0;
    }
}

//#include <set>
//
//// Expresiones regulares
//
//#include <regex>
//
//#include <sys/socket.h>
//#include <iostream>
//#include <stdio.h>
//#include <stdlib.h>
//#include <string.h>	/* needed for memset */
//#include <netinet/in.h>	/* needed for sockaddr_in */
//#include <netdb.h>
//#include <arpa/inet.h>
//#define PORT 54321
//#define BUFSIZE 1024
//
//#include "Cliente.h"
//
//using namespace std;
//
//int main(int argc, char **argv) {
//	struct sockaddr_in myaddr; /* our address */
//	struct sockaddr_in remaddr; /* remote address */
//	socklen_t addrlen = sizeof(remaddr); /* length of addresses */
//	int recvlen; /* # bytes received */
//	int fd; /* our socket */
//	int msgcnt = 0; /* count # of messages we received */
//	char buf[BUFSIZE]; /* receive buffer */
//
//	set<Cliente*> * clientes = new set<Cliente*>; //acá almaceno la información de cada cliente nuevo
//
//	/* create a UDP socket */
//
//	//AF_INET indica que uso una familia de direcciones IP
//	//SOCK_DGRAM el tipo de servicio que ofrece el socket; es para UDP
//	//el 0 indica que no hay modificaciones adicionales sobre el protocolo
//	if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
//		perror("cannot create socket\n");
//		return 0;
//	}
//
//	/* bind the socket to any valid IP address and a specific port */
//
//	memset((char *) &myaddr, 0, sizeof(myaddr));
//	myaddr.sin_family = AF_INET;
//	myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
//	myaddr.sin_port = htons(PORT);
//
//	if (bind(fd, (struct sockaddr *) &myaddr, sizeof(myaddr)) < 0) {
//		perror("bind failed");
//		return 0;
//	}
//	/* now loop, receiving data and printing what we received */
//	for (;;) {
//		printf("Esperando en puerto %d\n", PORT, "...");
//		recvlen = recvfrom(fd, buf, BUFSIZE, 0, (struct sockaddr *) &remaddr,
//				&addrlen);
//		if (recvlen > 0) {
//			buf[recvlen] = 0;
//			printf("Mensaje recibido: \"%s\" (%d bytes)\n", buf, recvlen);
//		} else
//			printf("El mensaje estaba vacío\n");
//		sprintf(buf, "ack %d", msgcnt++);
//		//testeo de regex
//		regex regex("^LOGIN\\s+<(.*)><CR>$");
//		smatch m;
//		string str(buf);
//		cout << regex_match(str, m, regex) << endl;
//		for (auto result : m) {
//			std::cout << result << std::endl;
//		}
//
//		printf("sending response \"%s\"\n", buf);
//		//envío respuesta
//		if (sendto(fd, buf, strlen(buf), 0, (struct sockaddr *) &remaddr,
//				addrlen) < 0)
//			perror("sendto");
//	}
//	/* never exits */
//}