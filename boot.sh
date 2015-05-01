#!/bin/bash

if [ $# -ne 2 ]; then
	echo "usage: $0 <master-public-IP> <private-key-file>"
	exit 1
fi

export MASTER_PUBLIC_IP=$1
MASTER_USER_HOST=ubuntu@$MASTER_PUBLIC_IP
PRIVATE_KEY_FILE=$2
MASTER_BOOT_DIR=/home/ubuntu/paas-boot

sed -e "s/MASTER_PUBLIC_IP/$MASTER_PUBLIC_IP/g" -e "s!PRIVATE_KEY_FILE!$PRIVATE_KEY_FILE!g" ansible_hosts.pattern > ansible_hosts
ansible-playbook -i ansible_hosts master-start.yml
scp -i $PRIVATE_KEY_FILE ansible_hosts slaves-start.yml slaves-stop.yml $MASTER_USER_HOST:$MASTER_BOOT_DIR
#ssh -i $PRIVATE_KEY_FILE $MASTER_USER_HOST "ansible-playbook -i $MASTER_BOOT_DIR/ansible_hosts $MASTER_BOOT_DIR/slaves-start.yml"

