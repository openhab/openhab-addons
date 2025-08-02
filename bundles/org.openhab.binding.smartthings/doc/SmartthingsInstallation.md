# Installation of Smartthings code

To use the Smartthings, openHAB binding code needs to be installed on the Smartthings Hub.  Currently the Smartthings code is bundled with the binding.

## Installation of artifacts on the Smartthings HUB

The following steps need to be done on the Smartthings hub using the web based [Smartthings developers tools](https://graph.api.smartthings.com/).

### Initial steps

These steps assume you already have a Smartthings Hub and have set it up. And, you have created an account.

1. Open the developers website using the link above.
1. Logon using the same email and password as on your Smartthings phone app.
1. Click on locations
1. Verify your hub is listed.

### Copying Smartthings files

The files are located in the GitHub [repository](https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.smartthings/contrib/smartthings).

The following files need to be deployed

- OpenHabAppV2 - This is a SmartApp that receives requests from openHAB and returns the needed data
- OpenHabDeviceHandler - This is a lower level module that provides a connection between openHAB and the Hub using the LAN connection

### Install OpenHabAppV2

1. Locate OpenHabAppV2.groovy in the /contrib/smartthings/SmartApps Directory.
1. Open OpenHabAppV2.groovy in an editor (Some program you can use to copy the contents to the clipboard)
1. Copy the contents to the clipboard
1. Using the Smartthings developers tools:
1. Logon, if you are not logged on
1. Select **My SmartApps**
1. Click on the **+ New SmartApp** near the top right
1. Click on the **From Code** tab
1. Paste the contents of the clipboard
1. Click on the **Create** button near the bottom left
1. Click on **Publish -> For Me**
1. The SmartApp is now ready

### Install OpenHabDeviceHandler

1. Locate OpenHabDeviceHandler.groovy in the /contrib/smartthings/DeviceHandlers Directory.
1. Open OpenHabDeviceHandler.groovy in an editor (Some program you can use to copy the contents to the clipboard)
1. Copy the contents to the clipboard
1. Using the Smartthings developers tools:
1. Select **My Device Handlers**
1. Click on the **+ Create New Device Handler** near the top right
1. Click on the **From Code** tab
1. Paste the contents of the clipboard
1. Click on the **Create** button near the bottom left
1. Click on **Publish -> For Me**
1. The Device Handler is now ready

### Create the Device

1. Using the Smartthings developers tools:
1. Select **My Devices**
1. Click on the **+ New Device** near the top right
1. Enter the following data in the form:
   - Name: OpenHabDevice
   - Label: OpenHabDevice
   - Device Network ID: This needs to be the MAC address of your OpenHAB server with no spaces or punctuation
   - Type: OpenHabDeviceHandler (This should be the last one on the list)
   - Location: (Select from the dropdown)
   - Hub: (Select from the dropdown)
1. Click on the **Create** button near the bottom left
1. In the Preferences section enter the following:
   - ip: (This is the IP address of your openHAB server)
   - mac: (This is the same as the Device Network ID but with : between segments
   - port: 8080 (This is the port of the openHAB application on your server)
   - Save the preferences

## Configuration in the Smartthings App

Next the App needs to be configured using the Smartthings App on your smartphone. These instructions are for the new app.

1. Start the Smartthings App on your phone
1. Select the menu (3 horizontal bars) in the upper left corner
1. Select **SmartApps**
1. Click the **+** (Add) in the upper right
1. Scroll to the bottom and select **OpenHabAppV2**
   - In the selection screen select the devices you want to interact with openHAB. **Warning** devices not enabled(lacking the check mark in the box for the specific device) will be **ignored** by openHAB.
   - Near the bottom of the screen is **Notify this virtual device**, click on it and select **OpenHabDevice**.
   - Finally click **Done** at the bottom of the screen.
