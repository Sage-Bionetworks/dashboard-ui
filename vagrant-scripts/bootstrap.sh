#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive

# All the commands will run as root by vagrant provisioning unless the user is explicitly set
# Add '/usr/local/bin' to root's $PATH for this session as programs installed there may be needed
export PATH=${PATH}:/usr/local/bin

# Some packages need to be installed locally for the user 'vagrant'
export TARGET_USER=vagrant

# Create ~/.bash_profile if it does not exist
su - ${TARGET_USER} -c "cd ~"
su - ${TARGET_USER} -c "bash /${TARGET_USER}/vagrant-scripts/bash-profile.sh"

# Add wheezy-backports to the sources list
cp --force /${TARGET_USER}/vagrant-scripts/sources.list /etc/apt/sources.list

# Debian maintenance
apt-get --quiet --yes autoremove
apt-get --quiet --yes autoclean
apt-get --quiet --yes update
apt-get --quiet --yes upgrade
apt-get --quiet --yes --target-release wheezy-backports update
apt-get --quiet --yes --target-release wheezy-backports upgrade
apt-get --quiet --yes autoremove
apt-get --quiet --yes autoclean

# Tools
apt-get --quiet --yes install wget curl g++ zip

# NFS Client
apt-get --quiet --yes --target-release install nfs-common

# Java
apt-get --quiet --yes --target-release wheezy-backports install openjdk-7-jdk

# Play
su - ${TARGET_USER} -c "cd ~"
su - ${TARGET_USER} -c "wget http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2.zip"
su - ${TARGET_USER} -c "rm -rf activator-1.3.2"
su - ${TARGET_USER} -c "unzip typesafe-activator-1.3.2.zip"
su - ${TARGET_USER} -c "rm typesafe-activator-1.3.2.zip"
su - ${TARGET_USER} -c "echo 'export PATH=${PATH}:~/activator-1.3.2' >> ~/.bash_profile"

# JavaScript
# As npm is not in Debian Wheezy, install it via nodejs
apt-get --quiet --yes --target-release wheezy-backports install nodejs
update-alternatives --install /usr/bin/node nodejs /usr/bin/nodejs 100
curl https://www.npmjs.com/install.sh | sudo clean=yes bash
npm install -g grunt-cli
npm install -g mocha

# Redis
apt-get --quiet --yes --target-release wheezy-backports install redis-server
# Comment out binding to specific IPs
sed 's/^[[:space:]]*bind[[:space:]]/# &/g' /etc/redis/redis.conf | tee /etc/redis/redis.conf
service redis-server restart
