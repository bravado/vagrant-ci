vagrant-ci
==========

This is a Vagrant, Ansible and Jenkins configuration setup.

Jenkins CI - http://jenkins-ci.org/

Vagrant - http://www.vagrantup.com/

Ansible - http://www.ansible.com/home

    $ vagrant up 

Point your browser to http://localhost:8080 to test jenkins.

If port 8080 is not available on the host, Vagrant will autocorrect the forwarding rule and use port 220x.

If you have some problem initializing Jenkins you can check the logs files and restart using the following commands:

    $ vagrant ssh
    $ tail -f /var/log/jenkins/jenkins.log
    $ sudo /etc/init.d/jenkins restart

TODO 

- Create virtual machine for private GIT repository 
- Write blog post 
	- How to migrate ant projects to maven ?
	- How to deploy on amazon and digital ocean ?

