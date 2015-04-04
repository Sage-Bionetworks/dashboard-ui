Dashboard UI
=====================================

Development
------------------

##### Prerequisites

1. Install VirtualBox and Vagrant.
2. Create the local configuration file `~/.dashboard/dashboard.conf`. **Make sure the file is accessible only by you.** In this file, youu will need pointers to the resources and their corresponding credentials.

##### How to test & run locally

1. Initialize Vagrant `vagrant up`.
2. Connect to the Vagrant box `vagrant ssh`.
3. In the Vagrant box, go to the shared project folder `cd /vagrant`.
4. Run tests `activator test`. 
5. To test the JavaScripts, `npm install`, then `grunt`.
6. Launch the Play console `activator`.
7. Then in the Play console, use the command `run` to start a local dashboard web server.
8. If you want to pull in real data, again within the Play console,
```
run -Daws.access.key=<prod-access-key> -Daws.secret.key=<prod-secret-key> -Daccess.record.bucket=<prod-bucket> -Dstack=prod
```

Deployment
------------------

##### AMI creation

1. [Dashboard AMI creation](https://gist.github.com/eric-wu/8658696)
2. [Nginx reverse proxy setup](https://gist.github.com/eric-wu/8483112) behind a elastic load balancer and in front of a Play web app.
3. Load balancer set up.
    1. `80 (HTTP) forwarding to 80 (HTTP)`
    2. `443 (HTTPS, Certificate: wildcard.synapse.org-blah) forwarding to 8080 (HTTP)`
4. Note on log locations
    1. Access logs: /var/log/nginx/access.log
    2. Application logs: [/path/to/app]/logs/application.log

##### Deploy manually

1. Generate a distribution package. Run the Play `dist` command.
2. Launch a m3.medium EC2 instance from the customized AMI.
3. Once the instance is active, upload the distribution package to the instance using `scp`.
4. Go to the EC2 instance, unpack the package, and launch the dashboard app. Example: `dashboard-ui-1.0-20140127/bin/dashboard-ui -Dstack=prod -Dhttp.port=9001 2> /dev/null &`.
5. After maybe 3 hours, cross-validate with the current dashboard.
6. Once validated, swap the instance at the dashboard load balancer.

Design
------------------

* Based on [Play](https://github.com/playframework/playframework) , [D3](https://github.com/mbostock/d3)
* [Design](https://github.com/eric-wu/dashboard/wiki)
* [Cache Layer](https://github.com/Sage-Bionetworks/dashboard)
