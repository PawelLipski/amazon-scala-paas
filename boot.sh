#!/bin/bash

if [ $# -ne 2 ]; then
	echo "usage: $0 <master-public-IP> <private-key-file>"
	exit 1
fi

MASTER_USER_HOST=ubuntu@$1
PRIVATE_KEY_FILE=$2
MASTER_BOOT_DIR=/home/ubuntu/paas-boot

ssh -i $PRIVATE_KEY_FILE $MASTER_USER_HOST "mkdir -p $MASTER_BOOT_DIR"
scp -i $PRIVATE_KEY_FILE ansible_hosts slave-start.yml slave-stop.yml $MASTER_USER_HOST:$MASTER_BOOT_DIR
#ssh $MASTER_USER_HOST "ansible-playbook -i $MASTER_BOOT_DIR/ansible_hosts $MASTER_BOOT_DIR/start.yml"

