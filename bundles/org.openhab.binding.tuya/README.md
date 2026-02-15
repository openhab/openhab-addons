# Tuya Binding

This add-on connects Tuya Wi-Fi devices with openHAB or compatible systems.
The control and status reporting is done on the local network.
Cloud access is only needed for discovery and initial connection.

Devices need to be connected to a Tuya account (Tuya Smart app or Smart Life app).
Each device has a unique "local key" (password/secret) which needs to be added during Thing creation.
It is highly recommended to use the discovery feature for that, but you can also sniff the local key with a MITM proxy during pairing.

Please note that only one local connection is allowed per device.
Using the app (or other tools like tuya-mqtt) and the binding in parallel is not supported by Tuya devices and will cause problems such as inability to discover the IP address and/or inability to control the devices.
The other app (and/or tuya-mqtt) must be closed in order for this binding to operate properly.

## Supported Things

There are two things: `project` and `tuyaDevice`.

The `project` Thing represents a Tuya developer portal cloud project (see below).
`project` things must be configured manually and are needed for discovery only.

`tuyaDevice` things represent a single device.
They can be configured manually or by discovery.

## Discovery

Discovery is supported for `tuyaDevice` things.
By using discovery all necessary settings of the device are retrieved from your cloud account.

## Thing Configuration

### `project`

First create and link a Tuya Developer Account:

- Go to `iot.tuya.com` (the Tuya developer portal) and create an account.
You can choose any credentials (email/password) you like (it is not necessary that they are the same as in the app).
After confirming your account, log in to your new account.
- On the left navigation bar, select "Cloud", then "Create new Cloud project" (upper right corner).
Enter a name (e.g. "My Smarthome"), select "Smart Home" for "Industry" and "Development Method".
For security reasons, select only the "Data Center" that your app is connected to (you can change that later if you select the wrong one).
Select "IoT Core", "Authorization" and "Device Status Notification" as APIs.
- You should be redirected to the "Overview" tab of your project.
Write down (or copy) "Access ID/Client ID" and "Access Secret/Client Secret" (you can always look it up in your account).
- In the upper menu bar, select the "Devices" tab, then go to "Link Tuya App Account" and link your app account.

The next steps are performed in openHAB's Main UI:

Add a `project` and enter your credentials (`username`/`password`, from the app — not your cloud account!) and the cloud project credentials (`accessId`/`accessSecret`).
The `countryCode` is the international dial prefix of the country you registered your app in (e.g. `49` for Germany or `43` for Austria).
Depending on the app you use, set `schema` to `tuyaSmart` (for the Tuya Smart app) or `smartLife` (for the Smart Life app).
The `dataCenter` needs to be set to the same value as in your IoT project.

The Thing should come online immediately.

If the Thing does not come online, check

- if you really used the app and not the developer portal credentials
- if you entered the correct country code (check in the app if you accidentally chose a wrong country)
- check if you selected the correct "Data Center" in your cloud project (you can select more than one for testing).

### `tuyaDevice`

The best way to configure a `tuyaDevice` is using the discovery service.

The mandatory parameters are `deviceId`, `productId` and `localKey`.
The `deviceId` is used to identify the device, the `productId` identifies the type of the device and the `localKey` is a kind of password for access control.
These parameters are set during discovery.
If you want to manually configure the device, you can also read those values from the cloud project above.

For line-powered devices on the same subnet, the `ip` address and `protocol` version are automatically detected.
Tuya devices announce their presence via UDP broadcast packets, which is usually not available in other subnets.
Battery-powered devices do not announce their presence at all.
If automatic protocol version detection does not work there is no clear rule how to determine if a device has protocol 3.3 or 3.1.
In this case it is recommended to start with 3.3 and watch the log file. If 3.3 does not work, try using 3.1.

Some devices do not automatically refresh channels or only refresh at fairly long intervals even though the data is sampled at a much higher rate (e.g., some power meters only send updates every 10 minutes but sample continuously).
The `pollingInterval` can be used to adjust how often channels are updated and can be set to `0` (off) or to any integer value of 10 seconds or higher. The default is 10 seconds.
Note that this has no practical effect on battery powered devices. These only wake up when they have something to say and then go straight back to sleep.

In case something is not working, please open an issue on [GitHub](https://github.com/openhab/openhab-addons/issues/new?title=[tuya]) and add TRACE level logs.

## Channels

Channels are added automatically based on device schemas on first startup.
The binding first tries to get it from a database of known device schemas.
If no schema is found a schema retrieved from the cloud during discovery is used (if applicable).

The device will change to OFFLINE status if no device schema could be determined.

Channels can also be added manually.
The available channel-types are `color`, `dimmer`, `number`, `string` and  `switch`.
Depending on the channel one or more parameters are available.
If a schema is available (which should be the case in most setups), these parameters are auto-configured.

All channels have at least the `dp` parameter which is used to identify the channel when communicating with the device.

### Type `color`

The `color` channel has a second optional parameter `dp2`.
This parameter identifies the ON/OFF switch that is usually available on color lights.

### Type `dimmer`

The `dimmer` channel has two additional mandatory parameters `min` and `max`, one optional parameter `dp2` and one advanced parameter `reversed`.
The `min` and `max` parameters define the range allowed for controlling the brightness (most common are 0-255 or 10-1000).
The `dp2` parameter identifies the ON/OFF switch that is usually available on dimmable lights.
The `reversed` parameter changes the direction of the scale (e.g. 0 becomes 100, 100 becomes 0).
It defaults to `false`.

### Type `number/quantity`

The `number` and `quantity` channels have two additional mandatory parameters `min` and `max`.
The `min` and `max` parameters define the range allowed (e.g. 0-86400 for turn-off "countdown").

### Type `string`

The `string` channel has one additional optional parameter `range`.
It contains a comma-separated list of command options for this channel (e.g. `white,colour,scene,music` for the "workMode" channel).

### Type `ir-code`

IR code types:

- `Tuya DIY-mode` - use learned codes from real remotes.

  Make a virtual remote control in DIY, learn virtual buttons.

- `Tuya Codes Library (check Advanced options)` - use codes from the template library.

  Make a virtual remote control from pre-defined type of devices.

  Select the Advanced checkbox to configure other parameters:
  - `irCode` - decoding parameter
  - `irSendDelay` - send delay parameter
  - `irCodeType` - code library type parameter

- `NEC` - IR Code in NEC format
- `Samsung` - IR Code in Samsung format.

**Additional options:**

- `Active Listening` - Device will always be in learning mode.
  After sending a command with a key code, the device stays in learning mode
- `DP Study Key` - **Advanced**. DP number for study key. Used to receive key codes in learning mode. Change at your own
  risk.

If the linked Item receives a command with a `Key Code` (code library parameter), the device sends the appropriate key code.

#### How to use an IR code in NEC format

Example, from Tasmota you need to use **_Data_** parameter, it can be with or without **_0x_**

```json
{"Time": "2023-07-05T18:17:42", "IrReceived": {"Protocol": "NEC", "Bits": 32, "Data": "0x10EFD02F"}}
```

Another example: use the **_hex_** parameter

```json
{ "type": "nec", "uint32": 284151855, "address": 8, "data": 11, "hex": "10EFD02F" }
```

#### How to get key codes without Tasmota or others

The channel can receive a learned key (auto-detects the format and puts the auto-detected code in the channel).

To start learning codes, add a new channel with type String, set DP = 1, and set Range to `send_ir,study,study_exit,study_key`.

Link an Item to this channel and send the command `study`.

The device will enter learning mode and will be able to receive codes from the remote control.

Press a button on the remote control and you will see the key code in the `ir-code` channel.

If the type of the `ir-code` channel is **_NEC_** or **_Samsung_**, you will see just a hex code.

If the type of the `ir-code` channel is **_Tuya DIY-mode_**, you will see the code format type and a hex code.

After pressing buttons and copying codes, assign the codes to the Item which controls the device (adjust State Description and Command Options as desired).

After receiving the key code, learning mode automatically continues until you send the `study_exit` command or send a key code via the Item containing a code.

## Troubleshooting

- If the `project` Thing is not coming `ONLINE`, check if you see your devices in the cloud account on `iot.tuya.com`.
If the list is empty, most likely you selected the wrong data center.
- Check if there are errors in the log and if you see messages like `Configuring IP address '192.168.1.100' for Thing 'tuya:tuya:tuyaDevice:bf3122fba012345fc9pqa'`.
If this is missing, try configuring the IP manually.
The MAC of your device can be found in the auto-discovered Thing properties (this helps to identify the device in your router).
- Provide TRACE level logs.
Type `log:set TRACE org.openhab.binding.tuya` on the Karaf console to enable TRACE logging.
Use `log:tail` to display the log.
You can revert to normal logging with `log:set DEFAULT org.openhab.binding.tuya`
- At least disable/enable the Thing when providing logs.
For most details better remove the device, use discovery and re-add the device.
Please use PasteBin or a similar service, do not use JPG or other images, they can't be analyzed properly.
Check that the log doesn't contain any credentials.
- Add the Thing configuration to your report (in the UI use the "Code" view).
