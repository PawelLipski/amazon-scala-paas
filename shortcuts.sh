
# Ansible

alias ans="ansible --private-key=~/AWS/aws-master-key.pem"
alias ansa='ans all'
alias ansp='ansible-playbook'

# Amazon

function sshec2() {
	ssh -i ~/AWS/aws-master-key.pem ubuntu@$1
}

function scpkeyec2() {
	scp -i ~/AWS/aws-master-key.pem ~/AWS/aws-master-key.pem ubuntu@$1:/home/ubuntu
}

