#!/usr/bin/env bash

set -xe
set -o pipefail

# Some sleeps go here to give yum time to do its thing. This to avoid
# " Existing lock /var/run/yum.pid: another copy is running as <pid>...""
sudo yum install -y -q git
sleep 5
sudo yum install -y -q docker && sudo service docker start
sleep 5
sudo chkconfig docker on
sudo groupadd docker || echo "Group already exists"
sudo usermod -aG docker $USER

# Install Java
sudo yum install -y -q java-1.8.0-openjdk.x86_64
sleep 5
sudo yum install -y -q java-sdk
