# myUplink Binding

The myUplink binding is used to get "live data" from from Nibe heat pumps without plugging any custom devices into your heat pump.
This avoids the risk of losing your warranty.
Instead data is retrieved from myUplink.
The myUplink API is the successor of the Nibe Uplink API.
This binding should in general be compatible with all heat pump models that support myUplink.
Read or write access is supported by all channels as exposed by the API.
Write access might only be available with a paid subscription for myUplink.
You will need to create credentials at <https://dev.myuplink.com/apps> in order to use this binding.

## Supported Things

This binding provides two thing types:

| Thing/Bridge        | Thing Type          | Description                                                       |
|---------------------|---------------------|-------------------------------------------------------------------|
| bridge              | account             | cloud connection to a myUplink user account                       |
| thing               | generic-device      | the physical heatpump which is connected to myUplink              |
## Discovery

When the `account` bridge is setup, the binding will discover all heatpumps within that account and also detect the specific channels supported by the model.

## Bridge Configuration

The following configuration parameters are available for the bridge:

| Configuration Parameter | Required | Description                                                                                                                                                             |
|-------------------------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| clientId                | yes      | The clientId to login at myUplink cloud service. This is some kind of UUID. Visit <https://dev.myuplink.com/apps> to generate login credentials.                        |
| clientSecret            | yes      | The secret which belongs to the clientId.                                                                                                                               |
| dataPollingInterval     | no       | Interval (seconds) in which live data values are retrieved from the Easee Cloud API. (default = 60)                                                                     |

## Thing Configuration

It is recommended to use auto discovery which does not require further configuration.
If manual configuration is preferred you need to specify configuration as below.

| Configuration Parameter | Required | Description                                                                                                            |
|-------------------------|----------|------------------------------------------------------------------------------------------------------------------------|
| deviceId                | yes      | The id of the heatpump that will be represented by this thing. Can be retrieved via API call or autodiscovery.         |
| systemId                | no       | The systemId of the heatpump. Only needed for "SmartHomeMode". Can be retrieved via API call or autodiscovery.         |

## Channels

The binding only supports channels which are explicitely exposed by the myUplink API.

Depending on your model and additional hardware the channels might be different.
Thus no list is provided here.

## Full Example

The configuration below is an example which could easily be adopted to your actual model.
Thing configuration (account and generic-device) is the same for all models.
Item configuration depends on your specific model and thus channels will have different IDs and/or channels might not exist for all models.

### `demo.things` Example

```java
Bridge myuplink:account:myAccount "myUplink" [
    clientId="c7c2f9a4-b960-448f-b00d-b8f30aff3324",
    clientSecret="471147114711ABCDEF133713371337AB",
    dataPollingInterval=55
    ] {
        Thing generic-device vvm320 "VVM320" [ deviceId="id taken from automatic discovery", systemId="id taken from automatic discovery" ]
    }
```

### `demo.items` Example

```java
Number                  NIBE_ADD_STATUS        "Status ZH [%s]"          { channel="myuplink:generic-device:myAccount:vvm320:49993" }
Number                  NIBE_COMP_STATUS       "Status Compr. [%s]"      { channel="myuplink:generic-device:myAccount:vvm320:44064" }
Number:Temperature      NIBE_SUPPLY            "Supply line"             { unit="°C", channel="myuplink:generic-device:myAccount:vvm320:40008" }
Number:Temperature      NIBE_RETURN            "Return line"             { unit="°C", channel="myuplink:generic-device:myAccount:vvm320:40012" }
Number:Energy           NIBE_HM_HEAT           "HM heating"              { unit="kWh", channel="myuplink:generic-device:myAccount:vvm320:44308" }
Number:Energy           NIBE_HM_HW             "HM hot water"            { unit="kWh", channel="myuplink:generic-device:myAccount:vvm320:44306" }
```
