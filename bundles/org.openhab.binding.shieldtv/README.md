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
| keystore         | text    | Location of the Java Keystore         | N/A     | no       | no       |
| keystorePassword | text    | Password of the Java Keystore         | N/A     | no       | no       |

```java
Thing shieldtv:shieldtv:livingroom [ ipAddress="192.168.1.2" ]
```

## Channels

| Channel    | Type   | Read/Write | Description                 |
|------------|--------|------------|-----------------------------|
| keypress   | String | RW         | Manual Key Press Entry      |
| pincode    | String | RW         | PIN Code Entry              |
| app        | String | RW         | App Control                 |

```java
String ShieldTV_KEYPRESS "KEYPRESS [%s]" { channel = "shieldtv:shieldtv:livingroom:keypress" }
String ShieldTV_PINCODE  "PINCODE [%s]" { channel = "shieldtv:shieldtv:livingroom:pincode" }
String ShieldTV_APP "APP [%s]" { channel = "shieldtv:shieldtv:livingroom:app" }

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

The list above causes an instantanious "press and release" of each button.  
If you would like to manually control the press and release of each you may append _PRESS and _RELEASE to the end of each.
(e.g. KEY_FORWARD_PRESS or KEY_FORWARD_RELEASE)

APP will display the currently active app as presented by the ShieldTV.  
You may also send it a command of the app package name (e.g. com.google.android.youtube.tv) to start/change-to that app.

## Pin Code Process

For the ShieldTV to be successfully accessed an on-screen PIN authentication is required on the first connection.  

To begin the PIN process, send the text "REQUEST" to the pincode channel while watching your ShiledTV.  A 6 digit PIN should be displayed on the screen.

To complete the PIN process, send the PIN displayed to the pincode channel.  The display should return back to where it was originally.

This completes the PIN process.  Upon reconnection (either from reconfiguration or a restart of OpenHAB), you should now see a message of "Login Successful" in openhab.log


## Full Example

```java
Thing shieldtv:shieldtv:livingroom [ ipAddress="192.168.1.2", ]
```

```java
String ShieldTV_KEYPRESS "KEYPRESS [%s]" { channel = "shieldtv:shieldtv:livingroom:keypress" }
String ShieldTV_PINCODE  "PINCODE [%s]" { channel = "shieldtv:shieldtv:livingroom:pincode" }
String ShieldTV_APP "APP [%s]" { channel = "shieldtv:shieldtv:livingroom:app" }

```

