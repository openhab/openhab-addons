# MSpa Binding

Connect your MSpa Pools with openHAB if you have them connected to your MSpa-Link IOS or Android App.

## Limitations

To get remote access the binding needs a token which is bound to your EMail address.
If the binding is running it will request a token which means it will _steal_ the access to your App!
If you login on your App again it will _steal_ the access to this binding.
Search for a solution is ongoing.

## Supported Things

- `account`: Bridge connecting to your account
- `pool`: Pool connected to your account

## Discovery

Bridge `account` needs to be setup manually with your credentials `email` and `password`
Your `pool` are detected automatically after the bridge goes ONLINE.
There's no automatic background scan.
If you connect a new pool start discovery manually.


## Thing Configuration

### `account` Bridge Configuration

| Name            | Type    | Description                           | Default | Required | 
|-----------------|---------|---------------------------------------|---------|----------|
| email           | text    | EMail address of your account         | N/A     | yes      |
| password        | text    | Password to access your account       | N/A     | yes      |
| region          | text    | Region where your country is located  | ROW     | yes      |

Region options

- ROW: Rest of world
- US: United States
- CH: China

### `pool` Thing Configuration

| Name              | Type      | Description                           | Default   | Required | Advanced   |
|-------------------|-----------|---------------------------------------|-----------|----------|------------|
| deviceId          | text      | EMail address of your account         | N/A       | yes      | no         |
| productId         | text      | Password to access your account       | N/A       | yes      | no         |
| refreshInterval   | integer   | Refresh interval in minutes           | 15        | yes      | yes        |

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
Bridge mspa:account:4711   "MSpa Account"       [ email="YOUR_MAIL_ADDRESS",password="YOUR_PASSWORD", region="ROW"] {
         Thing pool 4712    "MSpa Pool OLSO"    [ deviceId="YOUR_DEVICE_ID", productId="YOUR_PRODUCT_ID"]
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
