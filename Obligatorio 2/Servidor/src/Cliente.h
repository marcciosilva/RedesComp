/*
 * Cliente.h
 *
 *  Created on: Sep 14, 2015
 *      Author: marccio
 */

#ifndef CLIENTE_H_
#define CLIENTE_H_

// STL
#include <set>
#include <iterator>

#include <string>

#include <sys/socket.h>
#include <netinet/in.h>	/* needed for sockaddr_in */

using namespace std;

class Cliente {
private:
	string apodo;
	struct sockaddr_in remaddr; /* remote address */
	socklen_t addrlen = sizeof(remaddr); /* length of address */
public:
	Cliente(string apodo, struct sockaddr_in remaddr, socklen_t addrlen);
	virtual ~Cliente();
	socklen_t getAddrlen() const;
	const string& getApodo() const;
	const struct sockaddr_in& getRemaddr() const;
};

#endif /* CLIENTE_H_ */
