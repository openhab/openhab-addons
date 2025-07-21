# MSpa Binding

Connect your MSpa Pools with openHAB if you have them connected to your MSpa-Link IOS or Android app.

Check which account type fits best to connect your pool:

- one owner-account: you registered one account with MSpa-Link app. This will _steal_ your token and you're not able to operate openHAB and Smartphone MSpa-Link app in parallel.
- two owner-account: you need to register two accounts with different email addresses to avoid stealing token - one used for Smartphone app and one for openHAB.
- visitor-account: allow pool access with QR code provided by MSpa-Link app.

## Supported Things

- `owner-account`: Bridge connecting to your account with `email` and `password` credentials
- `visitor-account`: Bridge connecting to your account with `grantCode` credentials
- `pool`: Pool connected to your account

## Discovery

Bridge `owner-account` or `visitor-account` needs to be setup manually with your credentials.
Your `pool` is automatically detected after the bridge goes ONLINE.
There's no automatic background scan.
If you connect a new pool afterwards start discovery manually.

## Thing Configuration

### `owner-account` Bridge Configuration

| Name            | Type    | Description                           | Default | Required |
|-----------------|---------|---------------------------------------|---------|----------|
| email           | text    | Email address of your account         | N/A     | yes      |
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
| grantCode         | text    | Grant from QR code presented by MSpa-Link app       | N/A     | yes      |
| region            | text    | Region where your country is located                | ROW     | yes      |

The `visitorId` is generated if you create the first time a `visitor-account` via openHAB UI so it's not mandatory.
Once generated don't lose it e.g. when deleting the account thing because all grants are bound to this `visitorId`!
If you use textual configuration you need to generate your own `visitorId` as 16 digit hex lower case characters.

The `grantCode` is a QR code provided by MSpa-Link app.
On your main screen click on _the gear_ top left, _Devices_ and then _share the spa_ which presents you a QR code.
Scan it with a third party QR app and put the displayed String into the `grantCode` section and save configuration immediately.
This `grantCode` is valid for a limited time to grant access.

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
They are detected after successful bridge creation or after granting access.

The `refreshInterval` minimum possible value is 5 minutes.

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

### `demo.things` Example

```java
Bridge mspa:owner-account:4711  "MSpa Account"      [ email="YOUR_MAIL_ADDRESS", password="YOUR_PASSWORD", region="ROW"] {
         Thing pool 4712        "MSpa Pool OSLO"    [ deviceId="YOUR_DEVICE_ID", productId="YOUR_PRODUCT_ID"]
}
```

### `demo.items` Example

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
