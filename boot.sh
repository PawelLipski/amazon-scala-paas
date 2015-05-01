#!/bin/bash

if [ $# -ne 2 ]; then
	echo "usage: $0 <master-public-IP> <private-key-file>"
	exit 1
fi

MASTER_PUBLIC_IP=$1
MASTER_USER_HOST=ubuntu@$MASTER_PUBLIC_IP
PRIVATE_KEY_FILE=$2
MASTER_REPO_DIR=/home/ubuntu/paas-repo

sed -e "s/MASTER_PUBLIC_IP/$MASTER_PUBLIC_IP/g" -e "s!PRIVATE_KEY_FILE!$PRIVATE_KEY_FILE!g" ansible_hosts_control.pattern > ansible_hosts_control
#chmod a-x ansible_hosts
ansible-playbook -i ansible_hosts_control master-start.yml
scp -i $PRIVATE_KEY_FILE $PRIVATE_KEY_FILE $MASTER_USER_HOST:/home/ubuntu/ 2>/dev/null
#ssh -i $PRIVATE_KEY_FILE $MASTER_USER_HOST "chmod a-x $MASTER_BOOT_DIR/ansible_hosts"
ssh -i $PRIVATE_KEY_FILE $MASTER_USER_HOST "ansible-playbook -i $MASTER_REPO_DIR/ansible_hosts_master $MASTER_REPO_DIR/slaves-start.yml"

