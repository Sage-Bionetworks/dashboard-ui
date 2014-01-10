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

    [dashboard-ui] $ help play      // Shows the list of available commands.
    [dashboard-ui] $ reload         // Reloads the sbt script. If you change the build files, you need to run this.
    [dashboard-ui] $ update         // Updates the dependencies.
    [dashboard-ui] $ clean          // Cleans the build.
    [dashboard-ui] $ compile        // (Note this also compiles the JavaScript in this app.)
    [dashboard-ui] $ test           // Runs the Play unit tests and integration tests.
    [dashboard-ui] $ eclipse        // Generates Eclipse project files.
    [dashboard-ui] $ run            // Starts a local instance in test mode.
    [dashboard-ui] $ dist           // Creates a distribution packages.

To launch a local instance that reads the production S3 bucket for real metric data, run this command in the play console:

    [dashboard-ui] $ run -Daws.accessKeyId=<prod-access-key> -Daws.secretKey=<prod-secret-key> -Dprod=true

### Deployment

1. Generate a distribution package. Launch the Play console and run the "dist" command.
2. Launch a m1.small EC2 instance.
    1. Choose the latest dashboard AMI ("dashboard-single-instance-20131220")
    2. Tag it with a good name (e.g. "dashboard-20140110")
    3. Choose the security group "dashboard"
3. Once the instance is active, upload the distribution package to the instance.

        $ scp -i prod-key-pair.pem target/universal/dashboard.zip admin@ec2-52-192-252-222.compute-1.amazonaws.com:/home/admin

4. Go to the EC2 instance, unpack the package, and launch the dashboard app

        $ dashboard-ui-1.0-SNAPSHOT/bin/dashboard-ui -Daws.accessKeyId=<prod-access-key> -Daws.secretKey=<prod-secret>
        -DsynapseUsr=dashboard@sagebase.org -DsynapsePwd=<synapse-password> -Dprod=true -Dhttp.port=9001

5. Send the process to run in the background

        $ <ctrl-z>
        $ bg

6. After maybe 3 hours, cross-validate with the current dashboard
7. Once validated, swap CNAMEs at Route53
