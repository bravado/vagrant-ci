vagrant-ci
==========

This is a Vagrant, Ansible and Jenkins configuration setup.

Jenkins CI - http://jenkins-ci.org/

Vagrant - http://www.vagrantup.com/

Ansible - http://www.ansible.com/home

$ vagrant up 

Point your browser to http://localhost:8080 to test jenkins.

If you have some problem initializing Jenkins you can check the logs files and restart using the following commands:

$ vagrant ssh
$ tail -f /var/log/jenkins/jenkins.log
$ sudo /etc/init.d/jenkins restart

