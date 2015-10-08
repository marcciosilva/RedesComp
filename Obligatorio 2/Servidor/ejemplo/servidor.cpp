// Multicast Server
// written for LINUX
// Version 0.0.2
//
// Change: IP_MULTICAST_LOOP : Enable / Disable loopback for outgoing messages
// 
// Compile : gcc -o servidor servidor.c
//
// This code has NOT been tested
// 

#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <cstring>
#include <unistd.h>

#define MAXBUFSIZE 65536 // Max UDP Packet size is 64 Kbyte

int main()
{
   int sockMulticast, status, socklen;
   char buffer[MAXBUFSIZE];
   struct sockaddr_in saddr;
   struct in_addr iaddr;
   unsigned char ttl = 3;
   unsigned char one = 1;

   // set content of struct saddr and imreq to zero
   memset(&saddr, 0, sizeof(struct sockaddr_in));
   memset(&iaddr, 0, sizeof(struct in_addr));

   // open a UDP socket
   sockMulticast = socket(PF_INET, SOCK_DGRAM, 0);
   if ( sockMulticast < 0 )
     perror("Error creating socket");

   saddr.sin_family = PF_INET;
   saddr.sin_port = htons(0); // Use the first free port
   saddr.sin_addr.s_addr = htonl(INADDR_ANY); // bind socket to any interface
   status = bind(sockMulticast, (struct sockaddr *)&saddr, sizeof(struct sockaddr_in));

   if ( status < 0 )
     perror("Error binding socket to interface");

   iaddr.s_addr = INADDR_ANY; // use DEFAULT interface

   // Set the outgoing interface to DEFAULT
   setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_IF, &iaddr,
	      sizeof(struct in_addr));

   // Set multicast packet TTL to 3; default TTL is 1
   setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_TTL, &ttl,
	      sizeof(unsigned char));

   // send multicast traffic to myself too
   status = setsockopt(sockMulticast, IPPROTO_IP, IP_MULTICAST_LOOP,
		       &one, sizeof(unsigned char));

   // set destination multicast address
   saddr.sin_family = PF_INET;
   saddr.sin_addr.s_addr = inet_addr("225.5.4.3");
   saddr.sin_port = htons(4096);

   // put some data in buffer
   strcpy(buffer, "Hello world\n");

   socklen = sizeof(struct sockaddr_in);
   // send packet to socket
   status = sendto(sockMulticast, buffer, strlen(buffer), 0,
		     (struct sockaddr *)&saddr, socklen);

   // shutdown socket
   shutdown(sockMulticast, 2);
   // close socket
   

   return 0;
}
