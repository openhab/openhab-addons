# DahuaDoor Binding

This binding integrates Dahua VTO Villastation door controllers with openHAB, enabling doorbell notifications, camera snapshots, and remote door control.

## Supported Things

| Thing Type | Thing ID  | Description                                      |
| ---------- | --------- | ------------------------------------------------ |
| VTO2202    | `vto2202` | Dahua VTO2202 outdoor station with single button |
| VTO3211    | `vto3211` | Dahua VTO3211 outdoor station with dual buttons  |

## Discovery

Dahua door stations are automatically discovered on the local network using the DHIP UDP multicast discovery protocol. The discovered thing is pre-configured with the device's IP address as `hostname`.
`username` and `password` must be set manually after accepting the thing in the inbox.

**Note:** Auto-discovery relies on UDP multicast (`239.255.255.251:37810`) and therefore only works when openHAB and the Dahua devices are on the **same subnet**.
Devices in a different subnet or VLAN will not be found automatically and must be added manually.

## Thing Configuration

### VTO2202/VTO3211 Device

Single-button outdoor station.

| Parameter    | Type | Required | Description                                                                  |
| ------------ | ---- | -------- | ---------------------------------------------------------------------------- |
| hostname     | text | Yes      | Hostname or IP address of the device (e.g., 192.168.1.100)                   |
| username     | text | Yes      | Username to access the device                                                |
| password     | text | Yes      | Password to access the device                                                |
| snapshotPath | text | Yes      | Linux path where image files are stored (e.g., /var/lib/openhab/door-images) |

**Note:** Windows paths are not currently supported.

## Channels

### VTO2202 Channels (Single Button)

| Channel ID  | Type    | Read/Write | Description                                               |
| ----------- | ------- | ---------- | --------------------------------------------------------- |
| bell-button | Trigger | Read       | Triggers when doorbell button is pressed (event: PRESSED) |
| door-image  | Image   | Read       | Camera snapshot taken when doorbell is pressed            |
| open-door-1 | Switch  | Write      | Command to open door relay 1                              |
| open-door-2 | Switch  | Write      | Command to open door relay 2                              |

### VTO3211 Channels (Dual Button)

| Channel ID    | Type    | Read/Write | Description                                        |
| ------------- | ------- | ---------- | -------------------------------------------------- |
| bell-button-1 | Trigger | Read       | Triggers when button 1 is pressed (event: PRESSED) |
| bell-button-2 | Trigger | Read       | Triggers when button 2 is pressed (event: PRESSED) |
| door-image-1  | Image   | Read       | Camera snapshot when button 1 is pressed           |
| door-image-2  | Image   | Read       | Camera snapshot when button 2 is pressed           |
| open-door-1   | Switch  | Write      | Command to open door relay 1                       |
| open-door-2   | Switch  | Write      | Command to open door relay 2                       |

## Full Example

### VTO2202 Example (Single Button)

#### Thing Configuration

When discovered automatically, the thing ID is based on the device serial number (e.g., `abc1234xyz56789`).
For manual configuration, any unique ID can be used.

```java
Thing dahuadoor:vto2202:abc1234xyz56789 "Front Door Station" @ "Entrance" [
    hostname="192.168.1.100",
    username="admin",
    password="password123",
    snapshotPath="/var/lib/openhab/door-images"
]
```

#### Item Configuration

```java
Switch OpenFrontDoor "Open Front Door" <door> { channel="dahuadoor:vto2202:abc1234xyz56789:open-door-1" }
Image FrontDoorImage "Front Door Camera" <camera> { channel="dahuadoor:vto2202:abc1234xyz56789:door-image" }
```

#### Rule Configuration

Send smartphone notification with camera image when doorbell is pressed (requires openHAB Cloud Connector):

```java
rule "Doorbell Notification"
when
    Channel "dahuadoor:vto2202:abc1234xyz56789:bell-button" triggered PRESSED
then
    sendBroadcastNotification("Visitor at the door", "door", 
        "entrance", "Entrance", "door-notifications", null, 
        "item:FrontDoorImage", 
        "Open Door=command:OpenFrontDoor:ON", null)
end
```

### VTO3211 Example (Dual Button)

#### Thing Configuration

```java
Thing dahuadoor:vto3211:entrance "Entrance Station" @ "Entrance" [
    hostname="192.168.1.101",
    username="admin",
    password="password123",
    snapshotPath="/var/lib/openhab/door-images"
]
```

#### Item Configuration

```java
Switch OpenApartment1 "Open Apartment 1" <door> { channel="dahuadoor:vto3211:entrance:open-door-1" }
Switch OpenApartment2 "Open Apartment 2" <door> { channel="dahuadoor:vto3211:entrance:open-door-2" }
Image Apartment1Image "Apartment 1 Camera" <camera> { channel="dahuadoor:vto3211:entrance:door-image-1" }
Image Apartment2Image "Apartment 2 Camera" <camera> { channel="dahuadoor:vto3211:entrance:door-image-2" }
```

#### Rule Configuration

Send notifications for both buttons:

```java
rule "Apartment 1 Doorbell"
when
    Channel "dahuadoor:vto3211:entrance:bell-button-1" triggered PRESSED
then
    sendBroadcastNotification("Visitor at Apartment 1", "door", 
        "entrance", "Entrance", "door-notifications", null, 
        "item:Apartment1Image", 
        "Open Door=command:OpenApartment1:ON", null)
end

rule "Apartment 2 Doorbell"
when
    Channel "dahuadoor:vto3211:entrance:bell-button-2" triggered PRESSED
then
    sendBroadcastNotification("Visitor at Apartment 2", "door", 
        "entrance", "Entrance", "door-notifications", null, 
        "item:Apartment2Image", 
        "Open Door=command:OpenApartment2:ON", null)
end
```
