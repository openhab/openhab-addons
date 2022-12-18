# ShieldTV Binding

This binding emulates the Nvidia ShieldTV Android App to interact with an Nvidia ShieldTV for purposes of remote control.

## Supported Things

This binding supports a single thing type:

- **shieldtv** - The ShieldTV

## Discovery

Discovery is not currently part of this binding but is on the roadmap.  This will be updated as updates are committed.

## Binding Configuration

This binding does not require any special configuration files.  

This binding does require a PIN login process (documented below) upon first connection.

## Thing Configuration

There are three required fields to connect successfully to a ShieldTV.

| Name             | Type    | Description                           | Default | Required | Advanced |
|------------------|---------|---------------------------------------|---------|----------|----------|
| ipAddress        | text    | IP address of the device              | N/A     | yes      | no       |
| keystore         | text    | Location of the Java Keystore         | N/A     | yes      | no       |
| keystorePassword | text    | Password of the Java Keystore         | N/A     | yes      | no       |

```java
Thing shieldtv:shieldtv:livingroom [ ipAddress="192.168.1.2", keystore="/home/openhab/nvidia-livingroom.keystore", keystorePassword="secret" ]
```

## Channels

| Channel    | Type   | Read/Write | Description                 |
|------------|--------|------------|-----------------------------|
| keypress   | String | RW         | Manual Key Press Entry      |
| pincode    | String | RW         | PIN Code Entry              |
| devicename | String | RO         | Device Common Name          |
| currentapp | String | RO         | Currently Running App       |
| startapp   | String | RW         | Start App by Name           }

```java
String ShieldTV_KEYPRESS "KEYPRESS [%s]" { channel = "shieldtv:shieldtv:livingroom:keypress" }
String ShieldTV_PINCODE  "PINCODE [%s]" { channel = "shieldtv:shieldtv:livingroom:pincode" }
String ShieldTV_DEVICENAME "DEVICENAME [%s]" { channel = "shieldtv:shieldtv:livingroom:devicename" }
String ShieldTV_CURRENTAPP "CURRENTAPP [%s]" { channel = "shieldtv:shieldtv:livingroom:currentapp" }
String ShieldTV_STARTAPP "STARTAPP [%s]" { channel = "shieldtv:shieldtv:livingroom:startapp" }

```

KEYPRESS will accept the following commands as strings (case sensitive):

- KEY_UP
- KEY_DOWN
- KEY_RIGHT
- KEY_LEFT
- KEY_ENTER
- KEY_HOME
- KEY_BACK
- KEY_MENU
- KEY_PLAYPAUSE
- KEY_REWIND
- KEY_FORWARD
- KEY_POWER

## Pin Code Process

For the ShieldTV to be successfully accessed an on-screen PIN authentication is required on the first connection.  

The process is as follows:
NOTE: It is critical that the keystore name and password used here matches the keystore and keystorePassword configured on the thing.

From the cli: 
              openssl req -x509 -newkey rsa:2084 -keyout nvidia.key -out nvidia.crt -sha256 -days 365
              (you may accept all of the defaults, this will be disposed of after successful authentication)

              openssl pkcs12 -export -in nvidia.crt -inkey nvidia.key -out nvidia.p12 -name nvidia

              keytool -importkeystore -destkeystore nvidia.keystore -srckeystore nvidia.p12 -srcstoretype PKCS12 -srcstorepass secret -alias nvidia

Once this is completed, you can configure the thing and items as shown above.  Please have your CLI watching openhab.log for the new keys.

To begin the PIN process, send the text "REQUEST" to the pincode channel while watching your ShiledTV.  A 6 digit PIN should be displayed on the screen.

To complete the PIN process, send the PIN displayed to the pincode channel.  The display should return back to where it was originally.

At this point you should see a new private key and certificate in openhab.log.  Save the output to nvidia.key and nvidia.crt (overwriting the original data).

Delete the original .p12 and .keystore files.

From the cli: 
              openssl pkcs12 -export -in nvidia.crt -inkey nvidia.key -out nvidia.p12 -name nvidia

              keytool -importkeystore -destkeystore nvidia.keystore -srckeystore nvidia.p12 -srcstoretype PKCS12 -srcstorepass secret -alias nvidia

This completes the PIN process.  Upon reconnection (either from reconfiguration or a restart of OpenHAB), you should now see a message of "Login Successful" in openhab.log


## Full Example

```java
Thing shieldtv:shieldtv:livingroom [ ipAddress="192.168.1.2", keystore="/home/openhab/nvidia-livingroom.keystore", keystorePassword="secret" ]
```

```java
String ShieldTV_KEYPRESS "KEYPRESS [%s]" { channel = "shieldtv:shieldtv:livingroom:keypress" }
String ShieldTV_PINCODE  "PINCODE [%s]" { channel = "shieldtv:shieldtv:livingroom:pincode" }
String ShieldTV_DEVICENAME "DEVICENAME [%s]" { channel = "shieldtv:shieldtv:livingroom:devicename" }
String ShieldTV_CURRENTAPP "CURRENTAPP [%s]" { channel = "shieldtv:shieldtv:livingroom:currentapp" }
String ShieldTV_STARTAPP "STARTAPP [%s]" { channel = "shieldtv:shieldtv:livingroom:startapp" }

```

