# Development overview

## Build

    mvn clean install -pl :org.openhab.binding.webexteams
    cp target/org.openhab.binding.webexteams-3.3.0-SNAPSHOT.jar /openhab/addons/

If the above fails due to formatting, etc.. you can try to run this first:

    mvn spotless:apply

## Test

Restart openhab from karaf console:

    shutdown -r

## Configure logging

Log into karaf console:

    ssh -p 8101 openhab@localhost

Then configure logging:

    log:set DEBUG org.openhab.binding.webexteams

To view the logs, close the karaf console.  On linux, do:


## References

Openhab documentation: https://www.openhab.org/docs/
Binding development: https://www.openhab.org/docs/developer/bindings/

