# MSpa Binding

Connect your MSpa Pools with openHAB if you have them connected to your MSpa-Link IOS or Android App.

Check in beforehand how your pool shall be connected to openHAB
You can have 

- one owner-account: you registered one account with MSpa-Link App. This will steal your token and you're not able to operate openHAB and Smartphone MSpa-Link in parallel.
- two owner-account: you need to register two accounts with different email addresses to avoid stealing token - one used for Smartphone App and one for openHAB
- visitor-account: allow pool access with QR code provided by MSpa-Link App

## Supported Things

- `owner-account`: Bridge connecting to your account
- `visitor-account`: Bridge connecting to your account
- `pool`: Pool connected to your account

## Discovery

Bridge `owner-account` or `visitor-account` needs to be setup manually with your credentials.
Your `pool` are detected automatically after the bridge goes ONLINE.
There's no automatic background scan.
If you connect a new pool start discovery manually.


## Thing Configuration

### `owner-account` Bridge Configuration

| Name            | Type    | Description                           | Default | Required | 
|-----------------|---------|---------------------------------------|---------|----------|
| email           | text    | EMail address of your account         | N/A     | yes      |
| password        | text    | Password to access your account       | N/A     | yes      |
| region          | text    | Region where your country is located  | ROW     | yes      |

Region options

- ROW: Rest of world
- US: United States
- CH: China

### `visitor-account` Bridge Configuration

| Name              | Type    | Description                                         | Default | Required | 
|-------------------|---------|-----------------------------------------------------|---------|----------|
| visitorId         | text    | Random 16 digit id with lower case hex characters   | N/A     | no/yes   |
| grantCode         | text    | Grant from QR code presented by MSpa-Link App       | N/A     | yes      |
| region            | text    | Region where your country is located                | ROW     | yes      |

The `visitorId` is generated if you create the `visitor-account` via openHAB UI so it's not mandatory. 
Once generated don't loose it e.g. when deleting the account because all grants are bound to this Id!
If you use textual configuration you need to generate your own `visitorId` as 16 digit hex lower case characters.


The `grantCode` is a QR code provided by MSpa-Link App.
On your main screen click on _the gear_ top left, _Devices_ and then _share the spa_ which presents you a QR code.
Scan it with a third party QR App and put the displayed String into the `grantCode` section and save configuration immediately. 
This code is available for a limited time to grant access.

Region options

- ROW: Rest of world
- US: United States
- CH: China

### `pool` Thing Configuration

| Name              | Type      | Description                           | Default   | Required | Advanced   |
|-------------------|-----------|---------------------------------------|-----------|----------|------------|
| deviceId          | text      | Identification number of your device  | N/A       | yes      | no         |
| productId         | text      | Product Id of your device             | N/A       | yes      | no         |
| refreshInterval   | integer   | Refresh interval in minutes           | 15        | yes      | yes        |

The configuration parameters `deviceId` and `productId` cannot be determined manually in beforehand.
They are detected after succesful login or after granting access.

## Channels

Channels for `pool`

| Channel               | Type                  | Read/Write | Description                                          |
|-----------------------|-----------------------|------------|------------------------------------------------------|
| heater                | Switch                | RW         | Control water heating                                |
| temperature           | Number:Temperature    | R          | Current water temperature                            |
| target-temperature    | Number:Temperature    | RW         | Target water temperature                             |
| jet-stream            | Switch                | RW         | Control jet stream for massage                       |
| bubbles               | Switch                | RW         | Switch bubbles on/off                                |
| bubble-level          | Number                | RW         | Intensity of bubbles                                 |
| circulation           | Switch                | RW         | Water circulation for filtering                      |
| uvc                   | Switch                | RW         | Eliminate germs with Ultraviolet-C water cleaning    |
| ozone                 | Switch                | RW         | Disinfect with Ozone water cleaning                  |
| lock                  | Switch                | RW         | Lock physical panel for inputs                       |

`bubble-level` options

- 1 for Low
- 2 for Medium
- 3 for High

## Full Example

### Thing Configuration

```java
Bridge mspa:owner-account:4711  "MSpa Account"      [ email="YOUR_MAIL_ADDRESS",password="YOUR_PASSWORD", region="ROW"] {
         Thing pool 4712        "MSpa Pool OLSO"    [ deviceId="YOUR_DEVICE_ID", productId="YOUR_PRODUCT_ID"]
}
```

### Item Configuration

```java
Switch                  MSPA_OSLO_Heater                    {channel="mspa:pool:4711:4712:heater" }
Number:Temperature      MSPA_OSLO_Water_Temperature         {channel="mspa:pool:4711:4712:temperature" }
Number:Temperature      MSPA_OSLO_Target_Water_Temperature  {channel="mspa:pool:4711:4712:target-temperature" }
Switch                  MSPA_OSLO_Jet_Stream                {channel="mspa:pool:4711:4712:jet-stream" }
Switch                  MSPA_OSLO_Bubbles                   {channel="mspa:pool:4711:4712:bubbles" }
Number                  MSPA_OSLO_Bubble_Level              {channel="mspa:pool:4711:4712:bubble-level" }
Switch                  MSPA_OSLO_Circulation               {channel="mspa:pool:4711:4712:circulation" }
Switch                  MSPA_OSLO_UVC                       {channel="mspa:pool:4711:4712:uvc" }
Switch                  MSPA_OSLO_Ozone                     {channel="mspa:pool:4711:4712:ozone" }
Switch                  MSPA_OSLO_Lock                      {channel="mspa:pool:4711:4712:lock" }

```
