#include <cstdlib>
#include <vector>
//#include <sys/socket.h>
#include <netinet/in.h>
#include <time.h> 

using namespace std;

/* Cliente:
 * 1 thread para escribir/leer en multicast
 * 1 thread para escribir/leer en unicast
 
 * Servidor:
 * 1 thread para recibir y responder en unicast
 * 1 thread para recibir en multicast
 * 1 thread para enviar en multicast
 * 1 thread para los comandos
 */

// Estructura de datos compartida por todos los threads
struct cliente {
	char nick[64];
	in_addr ip;
	in_port_t puerto;
	time_t last_seen;
};

vector<int> clientes; 

int main(int argc, char** argv) {

	return 0;
}

