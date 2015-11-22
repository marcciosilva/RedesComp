#!/bin/sh

LB2_IP=200.0.0.4
REQUEST_COUNT=100

echo "Enviando $REQUEST_COUNT pedidos a LB2 ($LB2_IP)"

function get_reply_md5() {
   wget http://$1/ -O - 2>/dev/null | md5sum
}

# Get md5sums of each server's page
route add default gw 200.0.0.4
SERVER1_MD5=$(get_reply_md5 10.0.0.21)
SERVER2_MD5=$(get_reply_md5 10.0.0.22)
route del default

for ((i=1; i<=$REQUEST_COUNT; i++)); do
   REPLY_MD5=$(get_reply_md5 $LB2_IP)
   [ "$REPLY_MD5" = "$SERVER1_MD5" ] && echo "respuestas recibidas desde S21"
   [ "$REPLY_MD5" = "$SERVER2_MD5" ] && echo "respuestas recibidas desde S22"
done | sort | uniq -c

