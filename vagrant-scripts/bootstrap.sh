#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive

apt-get autoclean
apt-get autoremove

cp --force /vagrant/vagrant-scripts/sources.list /etc/apt/sources.list

apt-get --quiet --yes update
apt-get --quiet --yes --target-release wheezy-backports upgrade

# Tools
apt-get --quiet --yes install curl
apt-get --quiet --yes install g++
apt-get --quiet --yes install zip

# NFS Client
apt-get --quiet --yes --target-release wheezy-backports install nfs-common

# Java
apt-get --quiet --yes --target-release wheezy-backports install openjdk-7-jdk

# Play
su - vagrant -c "wget http://downloads.typesafe.com/typesafe-activator/1.2.10/typesafe-activator-1.2.10.zip"
su - vagrant -c "rm -rf activator-1.2.10"
su - vagrant -c "unzip typesafe-activator-1.2.10.zip"
su - vagrant -c "rm typesafe-activator-1.2.10.zip"
echo "export PATH=$PATH:/home/vagrant/activator-1.2.10" >> /etc/profile

# JavaScript
apt-get --quiet --yes --target-release wheezy-backports install nodejs
ln -s /usr/bin/nodejs /usr/bin/node
curl https://www.npmjs.org/install.sh | sudo clean=yes sh
npm install -g mocha
pushd .
su - vagrant -c "cd /vagrant"
su - vagrant -c "npm install jsdom"
su - vagrant -c "npm install jquery"
su - vagrant -c "npm install d3"
popd

# PostgreSQL
apt-get --quiet --yes --target-release wheezy-backports install postgresql
apt-get --quiet --yes --target-release wheezy-backports install postgresql-client
echo "listen_addresses = '*'" >> /etc/postgresql/9.1/main/postgresql.conf
echo "host all all 10.0.0.0/16 trust" >> /etc/postgresql/9.1/main/pg_hba.conf
su - postgres -c "psql -f /vagrant/vagrant-scripts/dw-bootstrap.sql"
service postgresql restart

# Redis
apt-get --quiet --yes --target-release wheezy-backports install redis-server
# Comment out binding to specific IPs
sed 's/^[[:space:]]*bind[[:space:]]/# &/g' /etc/redis/redis.conf | tee /etc/redis/redis.conf
service redis-server restart

