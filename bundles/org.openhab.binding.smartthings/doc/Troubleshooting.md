# Smartthings Binding Troubleshooting Guidelines
Below are some recommendations on resolving issues with things not working as expected 

## Device specific issues
If the binding is working for some devices but there is one device that doesn't seem to work then verify that the device is supported by checking the following in the PaperUI:
1. Open the PaperUI webpage
2. Select Configuration -> Bindings -> Smartthings
3. Verify that the thing you want to use is included in the list of Supported Things

If the device is listed then create a new topic in the [openHAB Community Add-ons -> Bindings](https://community.openhab.org/c/add-ons/bindings/) website. 

## Setting openHAB logs to Debug
You will need to edit the logging configuration file and set the log level for the Smartthings binding to debug.

The following assumes you are running on Linux or Raspbian

Follow these steps on your openHAB server:
1. If logged on to the server: cd /var/lib/openhab2/etc or if using Samba open file explorer to \\\\OPENHABIANPI\openHAB-userdata\etc
2. Edit the file org.ops4j.pax.logging.cfg
      * Just ** before ** the line "log4j2.logger.openhab.name = org.openhab" add the following lines:
      * log4j2.logger.smartthings.name=org.openhab.binding.smartthings
      * log4j2.logger.smartthings.level=DEBUG
      * Save the file
3. Restart the server (i.e. sudo systemctl restart openhab2.service)
      
## Viewing the openHAB logs
Viewing the logs is best done on the openHAB server where you can using the linux command "tail -f" to watch the log messages as they are created

Follow these steps on your openHAB server:
1. cd /var/log/openhab2 or if using Samba open file explorer to \\OPENHABIANPI\openHAB-log
2. The log file you want to see is openhab.log
3. Using unix "tail -f openhab.log"
4. Try the device that isn't working and look for log messages related to the device you are using 

## Viewing Smartthings logs
On the Smartthings hub all of the incoming messages and the responses are logged. Looking at these logs can be very informative.

To view these logs perform the following steps:
1. Using the Smartthings developers [IDE](https://graph.api.smartthings.com/):
2. Logon, if you are not logged on
3. Select **Live Logging** from the top menu bar. 
4. Try the device that isn't working and look for log messages related to the device you are having troubles with 
