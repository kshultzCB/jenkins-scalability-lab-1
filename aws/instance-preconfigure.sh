#!/usr/bin/env bash

set -xe
set -o pipefail

sudo yum install -y -q git
sudo yum install -y -q docker && sudo service docker start
sudo chkconfig docker on
sudo groupadd docker || echo "Group already exists"
sudo usermod -aG docker $USER

# Install Java
sudo yum install -y -q java-1.8.0-openjdk.x86_64
sudo yum install -y -q java-sdk
