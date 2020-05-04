# Smartthings Binding Troubleshooting Guidelines
If you find devices that don't work as expected please let me know what it is that doesn't work and I'll try to figure it out and make the necessary correction or enhancement. As I said in the README file I have a limited number of devices and have tested with those and am sure they work as expected.  If you are having trouble with a device that I don't own we will have to work together to diagnose what needs to be changed. To that end The Smartthings binding writes logs when unexpected conditions arise. The Smartthings OpenHab SmartApp writes it's own logs and sends error conditions back to the OpenHAB binding for logging.

## Setting OpenHAB logs to Debug
You will need to edit the logging configuration file and set the log level for the Smartthings binding to debug.

I am assuming you are running on Linux or Raspbian

Follow these steps on your OpenHAB server:
1. If logged on to the server: cd /var/lib/openhab2/etc or if using Samba open file explorer to \\OPENHABIANPI\openHAB-userdata\etc
2. Edit the file org.ops4j.pax.logging.cfg
      * Just ** before ** the line "log4j2.logger.openhab.name = org.openhab" add the following lines:
      * log4j2.logger.smartthings.name=org.openhab.binding.smartthings
      * log4j2.logger.smartthings.level=DEBUG
      * Save the file
3. Restart the server (i.e. sudo shutdown -r now)
      
## Viewing the OpenHAB logs
Viewing the logs is best done on the OpenHAB server where you can using the linux command "tail -f" to watch the log messages as they are created

Follow these steps on your OpenHAB server:
1. cd /var/log/openhab2 or if using Samba open file explorer to \\OPENHABIANPI\openHAB-log
2. The log file you want to see is openhab.log
3. Using unix "tail -f openhab.log"
4. Try the device that isn't working and send me log messages related to the device you are using 

## Viewing Smartthings logs
On the Smartthings hub I log all of the incoming messages and the responses. Looking at these logs can be very informative.

To view these logs perform the following steps:
1. Using the Smartthings developers tools:
2. Logon, if you are not logged on
3. Select **Live Logging** from the top menu bar. 
4. Try the device that isn't working and send me log messages related to the device you are using 

## Viewing the "Capabilities" supported 
In theory I support all of the [Capabilities](https://docs.smartthings.com/en/latest/capabilities-reference.html) defined in the Smartthings Capability Reference. But, I don't necessarily support all of the attributes and actions that are defined. You can look at what is supported by following these steps:

1. Using the Smartthings developers tools:
2. Logon, if you are not logged on
3. Select **My SmartApps** from the top menu bar.
4. Select the **OpenHabAppV2**
5. Around line 34 you will see the start of the Capabilities definition. It looks like: **@Field CAPABILITY_MAP = [**
6. Scroll down from there and look at the definition of the device you are interested in 

For example here is the switch definition:

    "switch": [
        name: "Switch",
        capability: "capability.switch",
        attributes: [
            "switch"
        ],
        action: "actionOnOff"
    ],

Make sure what you want to do is supported. If not, open an issue and let me know what you need and I will try to accommodate you. 


## Information I need to troubleshoot
To help me diagnose your issue please provide the following information:

1. What are you trying to do and what isn't working.
2. Do you have any devices working?
2. A description of the device: manufacture, model, etc.
3. Any relevant lines for this device from your **.things** file. Or, the thing definition from the Discovery Inbox
4. Any relevant lines for this device from your **.items** file.
5. Any relevant lines for this device from your **.sitemap** file

## Where to post requests for assistance.

Post your issue in the Issues section of the OpenHAB-smartthings GitHub repository.
