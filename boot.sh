#!/bin/bash

if [ $# -ne 2 ]; then
	echo "usage: $0 <master-public-IP> <private-key-file>"
	exit 1
fi

MASTER_USER_HOST=ubuntu@$1
PRIVATE_KEY_FILE=$2
MASTER_BOOT_DIR=/home/ubuntu/paas-boot

ansible-playbook -i ansible_hosts master-start.yml
scp -i $PRIVATE_KEY_FILE ansible_hosts slaves-start.yml slaves-stop.yml $MASTER_USER_HOST:$MASTER_BOOT_DIR
#ssh $MASTER_USER_HOST "ansible-playbook -i $MASTER_BOOT_DIR/ansible_hosts $MASTER_BOOT_DIR/slaves-start.yml"

