#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <map>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>
#include <unistd.h>

#define MAXBUFSIZE 65536 // Max UDP Packet size is 64 Kbyte

using namespace std;

map<string, struct sockaddr_in> clientes;
int sockUnicast, n;
struct sockaddr_in servUnicAddr, cliaddr, servMulticAddr;
socklen_t len;
char mesg[MAXBUFSIZE];

// Multicast
int sockMulticast, status, socklen;
struct sockaddr_in saddr;
struct in_addr iaddr;
unsigned char ttl = 3;
unsigned char one = 1;

bool esLogin() {
	char* regexpLogin = "LOGIN";
	//distingo entre tipos de mensaje
	int i = 0;
	int res;
	int len;
	char result[BUFSIZ];
	char err_buf[BUFSIZ];
	//        char* src = "hello world";
	//    const char* pattern = "(\\w+)";
	regex_t preg;

	regmatch_t pmatch[10];

	if ((res = regcomp(&preg, regexpLogin, REG_EXTENDED)) != 0) {
		regerror(res, &preg, err_buf, BUFSIZ);
		printf("regcomp: %s\n", err_buf);
		exit(res);
	}

	res = regexec(&preg, mesg, 10, pmatch, REG_NOTBOL);

	if (res == REG_NOMATCH) {
		printf("No es mensaje de login\n");
		return false;
	} else return true;
	regfree(&preg);
	return true;
}

bool apodoDisponible() {
	//si el apodo está disponible lo agrega al map de clientes
	char* regexpLogin = "LOGIN\\s(\\w+)";
	//distingo entre tipos de mensaje
	int i = 0;
	int res;
	int len;
	char result[BUFSIZ];
	char err_buf[BUFSIZ];
	//        char* src = "hello world";
	//    const char* pattern = "(\\w+)";
	regex_t preg;

	regmatch_t pmatch[10];

	if ((res = regcomp(&preg, regexpLogin, REG_EXTENDED)) != 0) {
		regerror(res, &preg, err_buf, BUFSIZ);
		printf("regcomp: %s\n", err_buf);
		exit(res);
	}

	res = regexec(&preg, mesg, 10, pmatch, REG_NOTBOL);

	if (res == REG_NOMATCH) {
		printf("Mensaje invalido\n");
		return false;
	}
	if (pmatch[1].rm_so != -1) {
		len = pmatch[i].rm_eo - pmatch[i].rm_so;
		memcpy(result, mesg + pmatch[i].rm_so, len);
		result[len] = 0;
		// chequeo que el nombre no esté en uso
		if (clientes.find(result) == clientes.end()) {
			string tmpCliente(result);
			clientes[tmpCliente] = cliaddr;
			return true;
		} else return false;
	} else return false;
	regfree(&preg);
	return true;
}

bool esLogout() {
	char* regexpLogin = "LOGOUT\n";
	//distingo entre tipos de mensaje
	int i = 0;
	int res;
	int len;
	char result[BUFSIZ];
	char err_buf[BUFSIZ];
	//        char* src = "hello world";
	//    const char* pattern = "(\\w+)";
	regex_t preg;

	regmatch_t pmatch[10];

	if ((res = regcomp(&preg, regexpLogin, REG_EXTENDED)) != 0) {
		regerror(res, &preg, err_buf, BUFSIZ);
		printf("regcomp: %s\n", err_buf);
		exit(res);
	}

	res = regexec(&preg, mesg, 10, pmatch, REG_NOTBOL);

	regfree(&preg);
	if (res == REG_NOMATCH) {
		return false;
	} else {
		// quito al cliente de mi lista de clientes
		map<string, struct sockaddr_in>::iterator it = clientes.begin();
		bool found = false;
		while (it != clientes.end() && !found) {
			//            sin.sin_addr == some_addr && sin.sin_port == some_port
			if (ntohl(it->second.sin_addr.s_addr) == ntohl(cliaddr.sin_addr.s_addr) &&
					ntohl(it->second.sin_port) == ntohl(cliaddr.sin_port)) {
				found = true;
			} else {
				it++;
			}
		}
		// asumo que es imposible que el cliente no esté a ésta altura del partido
		// pero igual agrego seguridad
		if (found) {
			clientes.erase(it);
		}
		return true;
	}
}

int main(int argc, char**argv) {


	sockUnicast = socket(AF_INET, SOCK_DGRAM, 0);

	servUnicAddr.sin_family = AF_INET;
	servUnicAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	servUnicAddr.sin_port = htons(54321);
	bind(sockUnicast, (struct sockaddr *) &servUnicAddr, sizeof (servUnicAddr));

	// Multicast
	// set content of struct saddr and imreq to zero
	memset(&saddr, 0, sizeof (struct sockaddr_in));
	memset(&iaddr, 0, sizeof (struct in_addr));

	// open a UDP socket
	sockMulticast = socket(PF_INET, SOCK_DGRAM, 0);
	if (sockMulticast < 0)
		perror("Error creating socket");

	saddr.sin_family = PF_INET;
	saddr.sin_port = htons(0); // Use the first free port
	saddr.sin_addr.s_addr = htonl(INADDR_ANY); // bind socket to any interface
	status = bind(sockMulticast, (struct sockaddr *) &saddr, sizeof (struct sockaddr_in));

	if (status < 0)
		perror("Error binding socket to interface");

	iaddr.s_addr = INADDR_ANY; // use DEFAULT interface

	// Set the outgoing interface to DEFAULT
	setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_IF, &iaddr,
			sizeof (struct in_addr));

	// Set multicast packet TTL to 3; default TTL is 1
	setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_TTL, &ttl,
			sizeof (unsigned char));

	// send multicast traffic to myself too
	status = setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_LOOP,
			&one, sizeof (unsigned char));

	// set destination multicast address
	saddr.sin_family = PF_INET;
	saddr.sin_addr.s_addr = inet_addr("225.5.4.3");
	saddr.sin_port = htons(4096);



	// Viejo
	//sockMulticast = socket(AF_INET, SOCK_DGRAM, 0);
	//memset(&sockMulticast, 0, sizeof (sockMulticast));
	//servMulticAddr.sin_family = AF_INET;
	//servMulticAddr.sin_addr.s_addr = inet_addr("225.5.4.3");
	//servMulticAddr.sin_port = htons(54322);
	//bind(sockMulticast, (struct sockaddr *) &servMulticAddr, sizeof (servMulticAddr));

	cout << "Servidor iniciado\n\n";
	char res[32] = {0};
	while (true) {

		len = sizeof (cliaddr);
		n = recvfrom(sockUnicast, mesg, MAXBUFSIZE, 0, (struct sockaddr *) &cliaddr, &len);

		// Envíó para multicast
		socklen = sizeof (struct sockaddr_in);
		sendto(sockMulticast, mesg, strlen(mesg), 0, (struct sockaddr *) &saddr, socklen);

		if (esLogin()) {
			if (apodoDisponible()) {
				//strcpy(res, "");
				cout << "Nombre disponible" << endl;
				strncpy(res, "OK\0", sizeof (res));
				sendto(sockUnicast, res, sizeof (res), 0, (struct sockaddr *) &cliaddr, sizeof (cliaddr));
			} else {
				//strcpy(res, "");
				cout << "Nombre no disponible" << endl;
				strncpy(res, "NOK\0", sizeof (res));
				sendto(sockUnicast, res, sizeof (res), 0, (struct sockaddr *) &cliaddr, sizeof (cliaddr));
			}
		} else if (esLogout()) {
			//strcpy(res, "");
			strncpy(res, "GOODBYE\0", sizeof (res));
			sendto(sockUnicast, res, sizeof (res), 0, (struct sockaddr *) &cliaddr, sizeof (cliaddr));
		}


		mesg[n] = 0;
		cout << "Mensaje recibido:\n";
		cout << mesg;
		cout << "--Fin--\n\n";
	}
	// shutdown socket
   shutdown(sockMulticast, 2);
   close(sockMulticast);
}
