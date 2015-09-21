#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>

int main(int argc, char**argv)
{
   int sockfd,n;
   struct sockaddr_in servaddr,cliaddr;
   socklen_t len;
   char mesg[1000];

   sockfd = socket(AF_INET,SOCK_DGRAM,IPPROTO_ICMP);

   servaddr.sin_family = AF_INET;
   servaddr.sin_addr.s_addr=htonl(INADDR_ANY);
   servaddr.sin_port=htons(54321);
   bind(sockfd,(struct sockaddr *)&servaddr,sizeof(servaddr));
   
   printf("Servidor iniciado\n\n");
   
   while (true){
      len = sizeof(cliaddr);
      n = recvfrom(sockfd,mesg,1000,0,(struct sockaddr *)&cliaddr,&len);
      sendto(sockfd,mesg,n,0,(struct sockaddr *)&cliaddr,sizeof(cliaddr));
      mesg[n] = 0;
      printf("Mensaje recibido:\n");
      printf(mesg);
	  printf("--Fin--\n\n");
   }
}
