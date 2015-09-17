/*
 * Cliente.cpp
 *
 *  Created on: Sep 14, 2015
 *      Author: marccio
 */

#include "Cliente.h"

Cliente::Cliente(string apodo, struct sockaddr_in remaddr, socklen_t addrlen) {
	this->apodo = apodo;
	this->remaddr = remaddr;
	this->addrlen = addrlen;
}

socklen_t Cliente::getAddrlen() const {
	return addrlen;
}

const string& Cliente::getApodo() const {
	return apodo;
}

const struct sockaddr_in& Cliente::getRemaddr() const {
	return remaddr;
}

Cliente::~Cliente() {
}
