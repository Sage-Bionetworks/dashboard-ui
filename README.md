Dashboard UI
=====================================

(Based on Play 2.2.1, D3 3.3.10)

### Development

#### Gradle Settings

(See https://github.com/Sage-Bionetworks/dashboard)

#### The Play Console

Development is largely done in the Play console.  Launching the Play console loads the sbt build script:

    $ cd /path/to/project/root
    $ play

Commonly used Play commands:

    [dashboard-ui] $ help play  // Shows the list of available commands.
    [dashboard-ui] $ reload     // Reloads the sbt script. Every time you change the build files, you need to run this.
    [dashboard-ui] $ update     // Updates the dependencies.
    [dashboard-ui] $ clean      // Cleans the build.
    [dashboard-ui] $ compile    // (Note this also compiles the JavaScript in this app.)
    [dashboard-ui] $ test       // Runs the Play unit tests and integration tests.
    [dashboard-ui] $ eclipse    // Generates Eclipse project files.
    [dashboard-ui] $ run        // Starts a local instance in test mode.
    [dashboard-ui] $ dist       // Creates a distribution packages.

To launch a local instance that reads the production S3 bucket for real metric data, run this command in the play console:

    [dashboard-ui] $ run -Daws.accessKeyId=<prod-access-key> -Daws.secretKey=<prod-secret-key> -Dprod=true

### Deployment

1. Generate a distribution package. Launch the Play console and run the "dist" command.
2. Launch a m1.small EC2 instance.
    1. Choose the latest dashboard AMI (e.g "dashboard-single-20140118")
    2. Tag it with a good name (e.g. "dashboard-single-20140127")
    3. Choose the security group "dashboard-single"
3. Once the instance is active, upload the distribution package to the instance.

        $ scp -i prod-key-pair.pem target/universal/dashboard.zip admin@ec2-52-192-252-222.compute-1.amazonaws.com:/home/admin

4. Go to the EC2 instance, unpack the package, and launch the dashboard app.

        $ dashboard-ui-1.0-20140127/bin/dashboard-ui -Daws.accessKeyId=<prod-access-key> -Daws.secretKey=<prod-secret>
        -DsynapseUsr=dashboard@sagebase.org -DsynapsePwd=<synapse-password> -Dprod=true -Dhttp.port=9001 2> /dev/null &

5. After maybe 3 hours, cross-validate with the current dashboard.
6. Once validated, swap the instance at the dashboard load balancer.

### AWS Notes

1. [Dashboard AMI creation](https://gist.github.com/eric-wu/8658696)
2. [Nginx reverse proxy setup](https://gist.github.com/eric-wu/8483112) behind a elastic load balancer and in front of a Play web app.
3. Log locations
    1. Access logs: /var/log/nginx/access.log
    2. Application logs: [/path/to/app]/logs/application.log
4. The AMIs, security groups, with "single" in the names, are for running a single dashboard instance where the Redis cache, the web app,
the reverse proxy all co-exist on a single host. But dashboard can easily configured to run on multiple hosts in a distributed manner.

