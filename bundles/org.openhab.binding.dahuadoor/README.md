# DahuaDoor Binding

This binding integrates Dahua VTO2202F Villastation door controllers with openHAB, enabling doorbell notifications, camera snapshots, and remote door control.

## Supported Things

| Thing Type | Thing ID        | Description                                      |
|------------|-----------------|--------------------------------------------------|
| VTO Device | `dahua_vto2202` | Dahua VTO2202F Villastation door unit with camera and door relays |

## Discovery

Automatic discovery is not supported.
Things must be manually configured.

## Thing Configuration

### VTO Device (`dahua_vto2202`)

| Parameter    | Type | Required | Default | Description                                                   |
|--------------|------|----------|---------|---------------------------------------------------------------|
| hostname     | text | Yes      | -       | Hostname or IP address of the device (e.g., 192.168.1.100)   |
| username     | text | Yes      | -       | Username to access the device                                 |
| password     | text | Yes      | -       | Password to access the device                                 |
| snapshotpath | text | Yes      | -       | Linux path where image files are stored (e.g., /var/lib/openhab/door-images) |

**Note:** Windows paths are not currently supported.

## Channels

### VTO Device Channels

| Channel ID   | Type    | Read/Write | Description                                        |
|--------------|---------|------------|----------------------------------------------------|
| bell-button  | Trigger | Read       | Triggers when doorbell button is pressed (event: PRESSED) |
| door-image   | Image   | Read       | Camera snapshot taken when doorbell is pressed     |
| open-door-1  | Switch  | Write      | Command to open door relay 1                       |
| open-door-2  | Switch  | Write      | Command to open door relay 2                       |

## Full Example

### Thing Configuration

```java
Thing dahuadoor:dahua_vto2202:frontdoor "Front Door Station" @ "Entrance" [
    hostname="192.168.1.100",
    username="admin",
    password="password123",
    snapshotpath="/var/lib/openhab/door-images"
]
```

### Item Configuration

```java
Switch OpenFrontDoor "Open Front Door" <door> { channel="dahuadoor:dahua_vto2202:frontdoor:open-door-1" }
Image FrontDoorImage "Front Door Camera" <camera> { channel="dahuadoor:dahua_vto2202:frontdoor:door-image" }
```

### Rule Configuration

Send smartphone notification with camera image when doorbell is pressed (requires openHAB Cloud Connector):

```java
rule "Doorbell Notification"
when
    Channel "dahuadoor:dahua_vto2202:frontdoor:bell-button" triggered PRESSED
then
    sendBroadcastNotification("Visitor at the door", "door", 
        "entrance", "Entrance", "door-notifications", null, 
        "item:FrontDoorImage", 
        "Open Door=command:OpenFrontDoor:ON", null)
end
```
