#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive

apt-get autoclean
apt-get autoremove

echo 'deb http://mirrors.kernel.org/debian wheezy-backports main' >> /etc/apt/sources.list
apt-get --quiet --yes update
apt-get --quiet --yes --target-release wheezy-backports upgrade

# Tools
apt-get --quiet --yet install curl
apt-get --quiet --yes install zip

# Java
apt-get --quiet --yes --target-release wheezy-backports install openjdk-7-jdk

# Play
su - vagrant -c "wget http://downloads.typesafe.com/play/2.2.4/play-2.2.4.zip"
su - vagrant -c "unzip play-2.2.4.zip"
su - vagrant -c "rm play-2.2.4.zip"
echo "export PATH=$PATH:/home/vagrant/play-2.2.4" >> /etc/profile

# JavaScript
apt-get --quiet --yes --target-release wheezy-backports install nodejs
ln -s /usr/bin/nodejs /usr/bin/node
curl https://www.npmjs.org/install.sh | sudo sh
npm install -g mocha

# PostgreSQL
apt-get --quiet --yes --target-release wheezy-backports install postgresql
apt-get --quiet --yes --target-release wheezy-backports install postgresql-client
echo "listen_addresses = '*'" >> /etc/postgresql/9.1/main/postgresql.conf
echo "host all all 10.0.0.0/16 trust" >> /etc/postgresql/9.1/main/pg_hba.conf
service postgresql restart

# Redis
apt-get --quiet --yes --target-release wheezy-backports install redis-server
# Comment out binding to specific IPs
sed 's/^[[:space:]]*bind[[:space:]]/# &/g' /etc/redis/redis.conf | tee /etc/redis/redis.conf
service redis-server restart

