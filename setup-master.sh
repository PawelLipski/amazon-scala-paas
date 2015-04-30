#!/bin/bash

if [ $# -ne 2 ]; then
	echo "usage: $0 <master-public-IP> <private-key-file>"
	exit 1
fi

MASTER_PUBLIC_IP=$1
PRIVATE_KEY_FILE=$2
MASTER_BOOT_DIR=/home/ubuntu/paas-boot

ssh -i $PRIVATE_KEY_FILE ubuntu@$MASTER_PUBLIC_IP "mkdir -p $MASTER_BOOT_DIR"
scp -i $PRIVATE_KEY_FILE ansible_hosts start.yml ubuntu@$MASTER_PUBLIC_IP:$MASTER_BOOT_DIR
#ssh -c "ansible-playbook $MASTER_BOOT_DIR/start.yml" $MASTER_PUBLIC_IP

