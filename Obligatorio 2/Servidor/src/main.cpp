#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include<map>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>

using namespace std;

map<string, struct sockaddr_in> clientes;
int sockfd, n;
struct sockaddr_in servaddr, cliaddr;
socklen_t len;
char mesg[1000];

bool esLogin() {
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

int main(int argc, char**argv) {


    sockfd = socket(AF_INET, SOCK_DGRAM, 0);

    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(54321);
    bind(sockfd, (struct sockaddr *) &servaddr, sizeof (servaddr));

    cout << "Servidor iniciado\n\n";
	char res[32] = {0};
    while (true) {

        len = sizeof (cliaddr);
        n = recvfrom(sockfd, mesg, 1000, 0, (struct sockaddr *) &cliaddr, &len);
		
        if (esLogin()) {
            if (apodoDisponible()) {
                //strcpy(res, "");
                cout << "Nombre disponible" << endl; //nombre disponible
                strcpy(res, "OK");
            } else {
                strcpy(res, "");
                cout << "Nombre no disponible" << endl; //nombre no disponible, ya en uso
                strcpy(res, "NOK");
            }
        }

        sendto(sockfd, res, n, 0, (struct sockaddr *) &cliaddr, sizeof (cliaddr));
        mesg[n] = 0;
        cout << "Mensaje recibido:\n";
        cout << mesg;
        cout << "--Fin--\n\n";
    }
}
