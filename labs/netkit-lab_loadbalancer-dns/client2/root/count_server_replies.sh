#!/bin/sh

SERVER_VIP=www.web.com
REQUEST_COUNT=100

echo "Sending 100 requests to $SERVER_VIP..."

function get_reply_md5() {
   # Avoid IPv6, because AAAA type queries may disturb
   # load balancing mechanisms
   wget -4 http://$1/ -O - 2>/dev/null | md5sum
}

# Get md5sums of each server's page
SERVER1_MD5=$(get_reply_md5 100.0.0.3)
SERVER2_MD5=$(get_reply_md5 100.0.0.4)
SERVER3_MD5=$(get_reply_md5 100.0.0.5)
SERVER4_MD5=$(get_reply_md5 200.0.0.6)
SERVER5_MD5=$(get_reply_md5 200.0.0.7)

for ((i=1; i<=$REQUEST_COUNT; i++)); do
   REPLY_MD5=$(get_reply_md5 $SERVER_VIP)
   [ "$REPLY_MD5" = "$SERVER1_MD5" ] && echo "replies received from server 1"
   [ "$REPLY_MD5" = "$SERVER2_MD5" ] && echo "replies received from server 2"
   [ "$REPLY_MD5" = "$SERVER3_MD5" ] && echo "replies received from server 3"
   [ "$REPLY_MD5" = "$SERVER4_MD5" ] && echo "replies received from server 4"
   [ "$REPLY_MD5" = "$SERVER5_MD5" ] && echo "replies received from server 5"
done | sort | uniq -c

