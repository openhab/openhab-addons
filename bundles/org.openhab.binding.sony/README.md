# Sony Binding

This binding is for Sony IP based product line including TVs, BDVs, AVRs, Blurays, Soundbars and Wireless Speakers.

## Supported Things

The following are the services that are available from different Sony devices.
Please note they are not exclusive of each other (many services are offered on a single device and offer different capabilities).
Feel free to mix and match as you see fit.

### Scalar (also known as the REST API)

The Scalar API is Sony's next generation API for discovery and control of the device.
This service has been implemented in most of the Sony products and has the same (and more) capabilities of all the other services combined.
If your device supports a Scalar thing, you should probably use it versus any of the other services.
The only downside is that it's a bit 'heavier' (depending on the device - will likely issue more calls) and is a bit more complicated to use (many, many channels are produced).

This service dynamically generates the channels based on the device.

For specifics - see [Scalar](README-SCALAR.md)

### Simple IP

The Simple IP protocol is a simplified version of IRCC and appears to be only supported on some models of Bravia TVs.
You must enable "Simple IP Control" on the devices (generally under `Settings->Network->Home Network->IP Control->Simple IP Control`) but once enabled - does not need any authentication.
The Simple IP control provides direct access to commonly used functions (channels, inputs, volume, etc) and provides full two-way communications (as things change on the device, openHAB will be notified immediately).

For specifics - see [Simple IP](README-SimpleIp.md)

### IRCC

Many Sony products (TVs, AV systems, disc players) provided an IRCC service that provides minimal control to the device and some minimal feedback (via polling) depending on the version.
From my research, their appears to be 5 versions of this service:

1. Not Specified - implemented on TVs and provides ONLY a command interface (i.e. sending of commands).
No feedback from the device is possible.
No status is available.
2. 1.0 - ???
3. 1.1 - ???
4. 1.2 - ???
5. 1.3 - implemented on blurays.
   
Provides a command interface, text field entry and status feedback (including disc information).
The status feedback is provided via polling of the device.

Please note that the IRCC service is fully undocumented and much of the work that has gone into this service is based on observations.

If you have a device that is reporting one of the "???" versions above, please post on the forum and I can give you directions on how we can document (and fix any issues) with those versions.

Please note that Sony has begun transitioning many of their products over to the Scalar API and the latest firmware updates have begun to disable this service.

For specifics - see [IRCC](README-IRCC.md)

### DIAL

The DIAL (DIscovery And Launch) allows you to discover the various applications available on the device and manage those applications (mainly to start or stop them).
This will apply to many of the smart tvs and bluray devices.
Generally you need to authenticate with IRCC before being able to use DIAL.
A channel will be created for each application (at startup only) and you can send ON to that channel to start the application and OFF to exit back to the main menu.

For specifics - see [DIAL](README-README-DIAL.md.md)

## Bluray Players

Please note that somy Bluray players have only a limited, partial implementation of SCALAR.  If you have a bluray player and scalar seems limited, you should try the DIAL/IRCC services as well.

## Application status

Sony has 'broken' the API that determines which application is currently running regardless if you use DIAL or Scalar services.
The API that determines whether an application is currently running ALWAYS returns 'stopped' (regardless if it's running or not).
Because of that - you cannnot rely on the application status and there is NO CURRENT WAY to determine if any application is running.

Both DIAL/Scalar will continue to check the status in case Sony fixes this in some later date - but as of this writing - there is NO WAY to determine application status.

## Device setup

To enable automation of your device may require changes on the device.
This section mainly applies to TVs as the other devices generally are setup correctly.
Unfortunately the location of the settings generally depend on the device and the firmware that is installed.
I'll mention the most common area for each below but do remember that it may differ on your device.

### Turn the device ON!!!

When a sony device is off, there are a number of 'things' that get turned off as well.  
You best action would be to turn the device ON when you are trying to set it up and bring it online for the first time.

1. IRCC/Scalar on Blurays will not be auto discovered if the device is off (however DIAL will be discovered).  
Both these services are turned off when the device is turned off.
2. Audio service on certain devices will either be turned off or limited in scope.  
If the audio service is off, you will either see no audio channels (volume, etc) or will be missing audio channels (like headphone volume for Bravias)

### Wireless Interface

If you are using the wireless interface on the device, you will *likely* lose the ability to power on the device with any of the services.  
Most sony devices will power down the wireless port when turning off or going into standby - making communication to that device impossible (and thus trying to power on the device impossible).
As of the when this was written, there is no known way to change this behaviour through device options or setup.


### Wake on LAN

To enable the device to wakeup based on network activity (WOL), go to `Settings->Network->Remote Start` and set to "ON".
This setting will cause the device to use more power (as it has to keep the ethernet port on always) but will generally allow you to turn on the device at any time.

Note: this will **likely** not work if your device is connected wirelessly and generally only affects physical ethernet ports.

### Enabling Remote Device Control

To enable openHAB to control your device, you'll need to set the device to allow remote control.
Go to `Settings->Network->Home network setup->Renderer->Render Function` and set it to "Enabled".

### Setting up the Authentication Mode

There are three major ways to authenticate to a Sony Device:

1. None - No authentication is needed (and openHAB should simply connect and work)
2. Normal - when openHAB registers with a device, a code is displayed on the device that needs to be entered into openHAB
3. Preshared - a predetermined key that is entered into openHAB

You can select your authentication mode by going to `Settings->Network->Home network setup->IP Control->Authentication` and selecting your mode.
I highly recommend the use of "Normal" mode.

Please note that their is a rare fourth option - some AVRs need to be put into a pairing mode prior to openHAB authentication.
This pairing mode acts similar to the "Normal" mode in that a code will be displayed on the AVR screen to be entered into openHAB.

Also note that generally AVRs/SoundBars/Wireless speakers need no authentication at all and will automatically come online.

See the authentication section below to understand how to use authenticate openHAB to the device.

## Discovery

This binding does attempt to discover Sony devices via UPNP.
Although this binding attempts to wake Sony devices (via wake on lan), some devices do not support WOL nor broadcast when they are turned off or sleeping.
If your devices does not automatically discovered, please turn the device on first and try again.
You may also need to turn on broadcasting via `Settings->Network->Remote Start` (on) - this setting has a side effect of turning on UPNP discovery.

### Enabling/Disabling services

By default, only the scalar service is enabled for discovery.
You can change the defaults by setting the following in the `conf/services/runtime.cfg` file:

```
discovery.sony-simpleip:background=false
discovery.sony-dial:background=false
discovery.sony-ircc:background=false
discovery.sony-scalar:background=true
```

## Authentication

#### Normal Key

A code request will request a code from the device and the device will respond by displaying new code on the screen.
Write this number down and then update the binding configuration with this code (FYI - you only have a limited time to do this - usually 30 or 60 seconds before that code expires).
Once you update the access code in the configuration, the binding will restart and a success message should appear on the device.

Specifically you should:

1. Update the "accessCode" configuration with the value "RQST".
The binding will then reload and send a request to the device.
2. The device will display a new code on the screen (and a countdown to expiration).
3. Update the "accessCode" configuration with the value shown on the screen.
The binding will then reload and ask the device to authorize with that code.
4. If successful, the device will show a success message and the binding should go online.
5. If unsuccessful, the code may have expired - start back at step 1.

If the device was auto-discovered, the "RQST" will automatically be entered once you approve the device (then you have 30-60 seconds to enter the code displayed on the screen in the PaperUI `Configuration->Things->the device->configuration->Access Code`).
If the code expired before you had a chance, simply double click on the "RQST" and press the OK button - that will force the binding to request a new code (and alternative if that doesn't work is to switch it to "1111" and then back to "RQST").

If you are manually setting up the configuration, saving the file will trigger the above process.

#### Pre Shared Key

A pre-shared key is a key that you have set on the device prior to discovery (generally `Settings->Network Control->IP Control->Pre Shared Key`).
If you have set this on the device and then set the appropriate accessCode in the configuration, no additional authentication is required and the binding should be able to connect to the device.

## Deactivation

If you have used the Normal Key authentication and wish to deactivate the addon from the TV (to either cleanup when uninstalling or to simply restart an new authentication process):

1. Go to `Settings->Network->Remote device settings->Deregister remote device`
2. Find and highlight the `openHAB (MediaRemote:00-11-22-33-44-55)` entry.
3. Select the `Deregister` button

If you have used a preshared key - simply choose a new key (this may affect other devices however since a preshared key is global).

## Thing Configuration

### IP Address Configuration

Any service can be setup by using just an IP address (or host name) - example: `192.168.1.104` in the deviceAddress field in configuration.
However, doing this will make certain assumptions about the device (ie path to services, port numbers, etc) that may not be correct for your device (should work for about 95% of the devices however).

If you plan on setting your device up in a .things file, I recommend autodiscovering it first and copy the URL to your things file.

There is one situation where you MUST use an IP address - if your device switches discovery ports commonly - then you must use a static IP address/host name.

### Common Configuration Options

The following is a list of common configuration options for all services

| Name               | Required | Default | Description                                                                                                   |
| ------------------ | -------- | ------- | ------------------------------------------------------------------------------------------------------------- |
| deviceAddress      | Yes (1)  | None    | The path to the descriptor file or the IP address/host name of the device                                     |
| deviceMacAddress   | No (2)   | eth0    | The device MAC address to use for wake on lan (WOL).                                                          |
| refresh            | No (3)   | 30      | The time, in seconds, to refresh some state from the device (only if the device supports retrieval of status) |
| checkStatusPolling | No       | 30      | The time, in seconds, to check the device status device                                                       |
| retryPolling       | No       | 10      | The time, in seconds, to retry connecting to the device                                                       |

1. See IP Address Configuration above
2. Only specify if the device support wake on lan (WOL)
3. Only specify if the device provides status information.
Set to negative to disable (-1).

```refresh``` is the time between checking the state of the device. 
This will query the device for it's current state (example: volume level, current input, etc) and update all associated channels. 
This is necessary if there are changes made by the device itself or if something else affects the device state outside of openHAB (such as using a remote).

```checkStatusPolling``` is the time between checking if we still have a valid connection to the device.
If a connection attempt cannot be made, the thing will be updated to OFFLINE and will start a reconnection attempt (see ```retryPolling```).

```retryPolling``` is the time between re-connection attempts.
If the thing goes OFFLINE (for any non-configuration error), reconnection attempts will be made.
Once the connection is successful, the thing will go ONLINE.

### Ignore these configuration options

The following 'configuration' options (as specified in the config XMLs) should **NEVER** be set as they are only set by the discovery process.

| Name                      | Description                                   |
| ------------------------- | --------------------------------------------- |
| discoveredMacAddress      | Don't set this - set deviceMacAddress instead |
| discoveredCommandsMapFile | Don't set this - set commandMapFile instead   |
| discoveredModelName       | Don't set this - set modelName instead        |

## Advanced Users Only

The following information is for more advanced users...

### Low power devices (PIs, etc)

This addon will try to only query information for the device to fulfill the information for channels you have linked.
However, if you've linked a great deal of channels (causing alot of requests to the device) and are running openHAB on a low power device - the polling time should be adjusted upwards to reduce the load on the PI.

### Separating the sony logging into its own file

To seperate all the sony logging information into a separate file, please do the following:

1. Edit userdata/etc/org.ops4j.pax.logging.cfg
2. Delete any lines that have "sony" in them
3. Add the following lines (at the end of the file), save and then restart openhab (you may want to adjust the log4j2.appender.sony.policies.size.size setting for something reasonable to your system)

```
# sony
log4j2.logger.sony.name = org.openhab.binding.sony
log4j2.logger.sony.level = DEBUG
log4j2.logger.sony.additivity = false
log4j2.logger.sony.appenderRefs = sony
log4j2.logger.sony.appenderRef.sony.ref = sony
log4j2.appender.sony.name = sony
log4j2.appender.sony.type = RollingRandomAccessFile
log4j2.appender.sony.fileName = ${openhab.logdir}/sony.log
log4j2.appender.sony.filePattern = ${openhab.logdir}/sony.log.%i
log4j2.appender.sony.immediateFlush = true
log4j2.appender.sony.append = true
log4j2.appender.sony.layout.type = PatternLayout
log4j2.appender.sony.layout.pattern = %d{dd-MMM-yyyy HH:mm:ss.SSS} [%-5.5p] [%-50.50c] - %m%n
log4j2.appender.sony.policies.type = Policies
log4j2.appender.sony.policies.size.type = SizeBasedTriggeringPolicy
log4j2.appender.sony.policies.size.size = 10MB
log4j2.appender.sony.strategy.type = DefaultRolloverStrategy
```
