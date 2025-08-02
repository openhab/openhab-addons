# Nest Binding

The Nest binding integrates devices by [Nest](https://store.google.com/us/category/connected_home?) using the [Smart Device Management](https://developers.google.com/nest/device-access/api) (SDM) API.

To be able to use the SDM API it is required to first [register](https://developers.google.com/nest/device-access/registration) and pay a US$5 non-refundable registration fee.

Because the SDM API runs on servers in the cloud, a connection with the Internet is required for sending and receiving information.
The binding uses HTTPS to connect to the APIs using port 443.
So make sure outbound connections to these ports are not blocked by a firewall.

> Note: This binding no longer supports the Works with Nest (WWN) API because it has been discontinued by Google.
See [Support for Works with Nest ending](https://support.google.com/googlenest/answer/9293712).

## Supported Things

The table below lists the Nest binding thing types:

| Things                                  | Description                                                            | Thing Type     |
|-----------------------------------------|------------------------------------------------------------------------|----------------|
| Nest Account                            | An account for using the Nest SDM REST API                             | sdm_account    |
| Nest Cam (Indoor, IQ, Outdoor), Dropcam | A Nest Cam registered with your account                                | sdm_camera     |
| Nest Hello Doorbell                     | A Nest Doorbell registered with your account                           | sdm_doorbell   |
| Nest Hub (Max)                          | A Nest Display registered with your account                            | sdm_display    |
| Nest Thermostat (E)                     | A Thermostat to control the various aspects of the house's HVAC system | sdm_thermostat |

The SDM API currently does not support Nest Protect devices.

To use the Nest SDM API, add an Account Thing using the UI and configure the required parameters.
After configuring an Account Thing, you can use it to discover the connected devices which are then added the Inbox.

## Account Configuration

### Google Account Requirement

To be able to use the SDM API it is required that you use a Google Account with your Nest devices.
To migrate to a Google account, follow the migration steps in the [Nest accounts FAQ](https://support.google.com/googlenest/answer/9297676?co=GENIE.Platform%3DiOS&hl=en&oco=0#accountmigration&accountmigration1&#accountmigration2&#accountmigration3&zippy=%2Chow-do-i-migrate-my-account)

### Configuration Parameters

These parameters configure which SDM project is accessed using the SDM API and configure the OAuth 2.0 client details used for accessing the project.

First a SDM project needs to be created and configured:

1. Register for device access by clicking the "Go to Device Access Console" button and follow the instructions on the [Device Access Registration](https://developers.google.com/nest/device-access/registration) page.
1. Create a new SDM project on the [Projects](https://console.nest.google.com/device-access/project-list) page
    1. Give your project a name so it is easily recognizable
    1. "Skip" entering the OAuth client ID for now
    1. If you want to download camera images using the binding, it is required to "Enable" events.
       Enabling events also allows for faster thermostat state updates.
       The binding only uses events when the Pub/Sub configuration parameters of the Nest SDM Account Thing are also configured.
    1. After clicking the "Create project" button, the SDM project details of the created project show
1. Copy and save the **Project ID** at the top of the page (e.g. `585de72e-968c-435c-b16a-31d1d3f76833`) somewhere

Now an OAuth 2.0 client is created and configured for using the SDM API by the binding:

1. Configure the "Publishing status" of your Google Cloud Platform to "Production" ([APIs & Services > OAuth consent screen](https://console.cloud.google.com/apis/credentials/consent)) so the OAuth 2.0 tokens do not expire after 2 weeks
1. Create a new client on the "Credentials" page ([APIs & Services > Credentials](https://console.cloud.google.com/apis/credentials)):
    1. Click the "Create Credentials" button at the top of the page
    1. Choose "OAuth client ID"
    1. As "Application type" choose "Web application"
    1. Give it a name so you can remember what it is used for (e.g. `Nest Binding SDM`)
    1. Add "<https://www.google.com>" to the "Authorized redirect URIs"
    1. Click "Create" to create the client
    1. Copy and save the generated **Client ID** (e.g. `12345-abcde.apps.googleusercontent.com`) and **Client Secret** (e.g. `xyz-987`) somewhere
1. Configure the SDM project to use the created client:
    1. Go the the SDM [Projects](https://console.nest.google.com/device-access/project-list) page
    1. Click on your SDM Project to show its details
    1. Scroll to "Project Info > OAuth client ID" and open the options menu (3 stacked dots) at the end of the line
    1. Select the "Edit" option
    1. Copy/paste the saved OAuth 2.0 Client ID here (e.g. `12345-abcde.apps.googleusercontent.com`)
    1. Click the "Save" button at the end of the line to update the project

Finally, an SDM Account Thing can be created to access the SDM project using the SDM API with the created client:

1. Create a new "Nest SDM Account" Thing in openHAB
1. Copy/paste the saved SDM **Project ID** to SDM group parameter in the SDM Account Thing configuration parameters (e.g. `585de72e-968c-435c-b16a-31d1d3f76833`)
1. Copy/paste the saved OAuth 2.0 **Client ID** to SDM group parameter (e.g. `12345-abcde.apps.googleusercontent.com`)
1. Copy/paste the saved OAuth 2.0 **Client Secret** to SDM group parameter (e.g. `xyz-987`)
1. Create an authorization code for the binding:
    1. Replace the **Project ID** and **Client ID** in the URL below with your SDM Project ID and SDM OAuth 2.0 Client ID and open the URL in a new browser tab:

       `https://nestservices.google.com/partnerconnections/<ProjectID>/auth?scope=https://www.googleapis.com/auth/sdm.service&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<ClientID>`

       For the example values used so far this is:

       `https://nestservices.google.com/partnerconnections/585de72e-968c-435c-b16a-31d1d3f76833/auth?scope=https://www.googleapis.com/auth/sdm.service&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=12345-abcde.apps.googleusercontent.com`
    1. Enable all the permissions you want to use with the binding and click "Next" to continue
    1. Login using your Google account when prompted
    1. On the "Google hasn't verified this app" page, click on "Advanced"
    1. Then click on "Go to ... (advanced)"
    1. Now "Allow" the SDM permissions and confirm your choices again by clicking "Allow"
    1. After your browser has been redirected to <https://www.google.com>, the **Authorization Code** will be set in the browser URL as value of the "code" URL query parameter
    1. Copy/paste the **Authorization Code** to the SDM group parameter in the openHAB Nest SDM Account Thing configuration
1. All required SDM Account Thing configuration parameters have now been entered so create it by clicking "Create Thing"

The SDM Account Thing should now be ONLINE and have as status description "Using periodic refresh".

Next click the "Enable" button on the [Smart Device Management API](https://console.cloud.google.com/apis/library/smartdevicemanagement.googleapis.com) page for your GCP project to enable the SDM API.

Now it should also be possible to use the configured account to discover your Nest devices via the Inbox.

You can monitor the SDM API using the Google Cloud Platform Console via [API & Services > Smart Device Management API](https://console.cloud.google.com/apis/api/smartdevicemanagement.googleapis.com/overview).

If you've made it this far, it should be easy to edit the SDM Account Thing again and update it so it can also use SDM Pub/Sub events. :-)

### Pub/Sub Configuration Parameters

After configuring the SDM configuration parameters, a SDM Account Thing can be updated so it can listen to SDM events using Pub/Sub.
This is required if you want to download camera images using the binding or to get faster thermostat state updates.

Enable Pub/Sub events in your SDM project:

1. Open your SDM project details using the [Projects](https://console.nest.google.com/device-access/project-list) page
1. Scroll to "Project Info > Pub/Sub topic" and check if it is set to "Enabled"
1. If it is set to "Disabled", enable events:
    1. Open the options menu (3 stacked dots) at the end of the line
    1. Select the "Edit" option
    1. Check the "Enable events" option
    1. Click the "Save" button at the end of the line to update the project
    1. Make a note of the **Pub/Sub topic** from the SDM configuration page

Lookup your Google Cloud Platform (GCP) Project ID:

1. Open the [IAM & Admin > Settings](https://console.cloud.google.com/iam-admin/settings)
1. Copy and save the GCP **Project ID** (e.g. `openhab-12345`)

Create a GCP Pub/Sub subscription in your GCP project:

1. Open the [Subscriptions Page > Create](https://console.cloud.google.com/cloudpubsub/subscription/create)
1. Select a unique name for the **Subscription ID** that uniquely identifies the Pub/Sub subscription used by the binding
1. Select "Pull" as the Delivery Type
1. Enter the **topic name** previously noted from the SDM project
1. Click "Create" to create the subscription

Next an OAuth 2.0 client is created which is used to create a Pub/Sub subscription for listening to SDM events by the binding:

1. Open the "Credentials" page ([APIs & Services > Credentials](https://console.cloud.google.com/apis/credentials)):
1. Click the "Create Credentials" button at the top of the page
1. Choose "OAuth client ID"
1. As "Application type" choose "Web application"
1. Give it a name so you can remember what it is used for (e.g. `Nest Binding Pub/Sub`)
1. Add "<https://www.google.com>" to the "Authorized redirect URIs"
1. Click "Create" to create the client
1. Copy and save the generated **Client ID** (e.g. `67890-fghij.apps.googleusercontent.com`) and **Client Secret** (e.g. `uvw-654`) somewhere

Finally, the existing SDM Account Thing can be updated so it can subscribe to SDM events:

1. Open the configuration details of your existing "Nest SDM Account" Thing in openHAB
1. Copy/paste the saved GCP **Project ID** to Pub/Sub group parameter (e.g. `openhab-123`)
1. Enter the name of the previously chosen **Subscription ID** that uniquely identifies the Pub/Sub subscription used by the binding

   > Must be 3-255 characters, start with a letter, and contain only the following characters: letters, numbers, dashes (-), periods (.), underscores (_), tildes (~), percents (%) or plus signs (+). Cannot start with  goog.
1. Copy/paste the saved OAuth 2.0 **Client ID** to Pub/Sub group parameter (e.g. `67890-fghij.apps.googleusercontent.com`)
1. Copy/paste the saved OAuth 2.0 **Client Secret** to Pub/Sub group parameter (e.g. `uvw-654`)
1. Create an authorization code for the binding:
    1. Replace the **Client ID** in the URL below with your Pub/Sub OAuth 2.0 Client ID and open the URL in a new browser tab:

       `https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/pubsub&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<ClientID>`

       For the example client this is:

       `https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/pubsub&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=67890-fghij.apps.googleusercontent.com`
    1. Login using your Google account when prompted
    1. On the "Google hasn't verified this app" page, click on "Advanced"
    1. Then click on "Go to ... (advanced)"
    1. Now "Allow" the Pub/Sub permissions and confirm your choices again by clicking "Allow"
    1. After your browser has been redirected to <https://www.google.com>, the **Authorization Code** will be set in the browser URL as value of the "code" URL query parameter
    1. Copy/paste the **Authorization Code** to the Pub/Sub group parameter in the openHAB Nest SDM Account Thing configuration
1. All required Pub/Sub Account Thing configuration parameters have now been entered so click "Save" to update the SDM Account Thing configuration

The SDM Account Thing should now be ONLINE and have as status description "Using periodic refresh and Pub/Sub".

The created subscription can also be monitored using the Google Cloud Platform Console via [Pub/Sub > Subscriptions](https://console.cloud.google.com/cloudpubsub/subscription/list).

## SDM Device Configuration

| Configuration Parameter | Required | Default | Description                                                               |
|-------------------------|----------|---------|---------------------------------------------------------------------------|
| deviceId                | X        |         | Identifies the device in the SDM API                                      |
| refreshInterval         |          | 300     | This is refresh interval in seconds to update the Nest device information |

Decreasing the `refreshInterval` may cause issues when you have a lot of devices connected because it may cause API rate limits to be exceeded.
You may want to decrease the `refreshInterval` for a Thermostat if Pub/Sub events have not been configured to provide state updating.

## Discovery

The binding will discover all Nest Things from your account when you add and configure a Nest Account Thing.

## Channels

### Account Channels

The account Thing Types do not have any channels.

### Camera/Display/Doorbell Channels

The state of these channels is based on Pub/Sub events sent by the SDM API.
So make sure the Pub/Sub account details are properly configured in the `sdm_account`.

| Channel Type ID                  | Item Type | Description                                         | Read Write |
|----------------------------------|-----------|-----------------------------------------------------|:----------:|
| chime_event#image                | Image     | Static image based on a chime event                 |      R     |
| chime_event#timestamp            | DateTime  | The last time that the door chime was pressed       |      R     |
| live_stream#current_token        | String    | Live stream current token value                     |      R     |
| live_stream#expiration_timestamp | DateTime  | Live stream token expiration time                   |      R     |
| live_stream#extension_token      | String    | Live stream token extension value                   |      R     |
| live_stream#url                  | String    | The RTSP video stream URL for the most recent event |      R     |
| motion_event#image               | Image     | Static image based on a motion event                |      R     |
| motion_event#timestamp           | DateTime  | The last time that motion was detected              |      R     |
| person_event#image               | Image     | Static image based on a person event                |      R     |
| person_event#timestamp           | DateTime  | The last time that a person was detected            |      R     |
| sound_event#image                | Image     | Static image based on a sound event                 |      R     |
| sound_event#timestamp            | DateTime  | The last time that a sound was detected             |      R     |

The `chime_event` group channels only exist for doorbell Things.
Each image channel has the `imageWidth` and `imageHeight` configuration parameters that can be used for configuring the image size in pixels.
The maximum camera resolution is listed as `maxImageResolution` property in the Thing properties.

### Thermostat Channels

| Channel Type ID     | Item Type            | Description                                                            | Read Write |
|---------------------|----------------------|------------------------------------------------------------------------|:----------:|
| ambient_humidity    | Number:Dimensionless | Lists the current ambient humidity percentage from the thermostat      |      R     |
| ambient_temperature | Number:Temperature   | Lists the current ambient temperature from the thermostat              |      R     |
| current_eco_mode    | String               | Lists the current eco mode from the thermostat (OFF, MANUAL_ECO)       |     R/W    |
| current_mode        | String               | Lists the current mode from the thermostat (OFF, HEAT, COOL, HEATCOOL) |     R/W    |
| fan_timer_mode      | Switch               | Lists the current fan timer mode                                       |     R/W    |
| fan_timer_timeout   | DateTime             | Timestamp at which timer mode turns OFF                                |     R/W    |
| hvac_status         | String               | Provides the thermostat HVAC Status (OFF, HEATING, COOLING)            |      R     |
| maximum_temperature | Number:Temperature   | Lists the maximum temperature setting from the thermostat              |     R/W    |
| minimum_temperature | Number:Temperature   | Lists the target temperature setting from the thermostat               |     R/W    |
| target_temperature  | Number:Temperature   | Lists the target temperature setting from the thermostat               |     R/W    |
| temperature_cool    | Number:Temperature   | Lists the heat temperature Setting from the thermostat                 |      R     |
| temperature_heat    | Number:Temperature   | Lists the heat temperature setting from the thermostat                 |      R     |

The `fan_timer_mode` channel has a `fanTimerDuration` configuration parameter that can be used for configuring how long the fan is ON before it is switched OFF (1s to 43200s).
Similarly, when a DateTime command is sent to the `fan_timer_timeout` channel, the fan timer is switched ON and runs until the timestamp in the command (min now+1s, max now+43200s).

## Example

You can use the discovery functionality of the binding to obtain the deviceId and structureId values for defining Nest things in files.

### sdm-demo.things

```java
Bridge nest:sdm_account:demo_sdm_account [ sdmProjectId="585de72e-968c-435c-b16a-31d1d3f76833", sdmClientId="12345-abcde.apps.googleusercontent.com", sdmClientSecret="xyz-987", sdmAuthorizationCode="xkkY3qYtfZCzaXCcPxpOELUW8EhgiSMD3n9jmzJ3m0yerkQpVRdj5vqWRjMSIG", pubsubProjectId="openhab-12345", pubsubSubscriptionId="nest-sdm-events", pubsubClientId="67890-fghij.apps.googleusercontent.com", pubsubClientSecret="uvw-654", pubsubAuthorizationCode="tASfQq7gn6sfbUSbwRufbMI0BYDzh1d7MBG2G7vdZpbhjmZfwDp5MkeaX0iMxn" ] {
    Thing sdm_camera       fish_cam          [ deviceId="AVPHwTQCAhersqmQ3IXwyqSX-XyuVZXoiNSNPeHdIMKgYpYZolNP4S9LS5QDF2LeuM3BQcpBh_fOEZYxkeH6eoQdWEELqi" ] {
        Channels:
            Image : motion_event#image [ imageHeight=1080 ]
            Image : person_event#image [ imageWidth=1920 ]
            Image : sound_event#image  [ imageHeight=1080 ]
    }
    Thing sdm_doorbell     front_door        [ deviceId="AVPHws4JWeIzZlru3DSxXoKnIgPntKpzax7a1Zwms8H0-HaRet2pTdTCPOTBZ74YDzYqq7w6XpEPwOTkBXtf4KCJ4nq9hq" ] {
        Channels:
            Image : chime_event#image  [ imageWidth=1920 ]
    }
    Thing sdm_display      kitchen_hub       [ deviceId="AVPHw64dWG5CcAJdDNzBbHWgu91l4v8WA4CsJqgtrvMS3QrbDnurB0_WzZEwpcWaw8Y9rLEQXW0avEwCjTd40Gmia6ussU" ]
    Thing sdm_thermostat   living_thermostat [ deviceId="AVPHwQum_bx9LmiRfv6jv5qPcKho0vHx2HqqMUvXP3TD-TTDCJebbzkegpRMozU5t7GSeTQIzxdH2LYDsZO8RClcGj7CCT", refreshInterval=180 ] {
        Channels:
            Image : fan_timer_mode     [ fanTimerDuration=7200 ]
    }
}
```

### sdm-demo.items

```java
/* SDM Doorbell */
Image    Doorbell_Chime_Image      "Chime Image"                                            { channel="nest:sdm_doorbell:demo_sdm_account:front_door:chime_event#image" }
DateTime Doorbell_Chime_Timestamp  "Chime Timestamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"  { channel="nest:sdm_doorbell:demo_sdm_account:front_door:chime_event#timestamp" }
String   Doorbell_Stream_Token     "Stream Token [%s]"                                      { channel="nest:sdm_doorbell:demo_sdm_account:front_door:live_stream#current_token" }
DateTime Doorbell_Stream_Timestamp "Stream Timestamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" { channel="nest:sdm_doorbell:demo_sdm_account:front_door:live_stream#expiration_timestamp" }
String   Doorbell_Stream_Ext_Token "Stream Extension Token [%s]"                            { channel="nest:sdm_doorbell:demo_sdm_account:front_door:live_stream#extension_token" }
String   Doorbell_Stream_URL       "Stream Extension URL [%s]"                              { channel="nest:sdm_doorbell:demo_sdm_account:front_door:live_stream#url" }
Image    Doorbell_Motion_Image     "Motion Image"                                           { channel="nest:sdm_doorbell:demo_sdm_account:front_door:motion_event#image" }
DateTime Doorbell_Motion_Timestamp "Motion Timestamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" { channel="nest:sdm_doorbell:demo_sdm_account:front_door:motion_event#timestamp" }
Image    Doorbell_Person_Image     "Person Image"                                           { channel="nest:sdm_doorbell:demo_sdm_account:front_door:person_event#image" }
DateTime Doorbell_Person_Timestamp "Person Timestamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" { channel="nest:sdm_doorbell:demo_sdm_account:front_door:person_event#timestamp" }
Image    Doorbell_Sound_Image      "Sound Image"                                            { channel="nest:sdm_doorbell:demo_sdm_account:front_door:sound_event#image" }
DateTime Doorbell_Sound_Timestamp  "Sound Timestamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"  { channel="nest:sdm_doorbell:demo_sdm_account:front_door:sound_event#timestamp" }

/* SDM Thermostat */
Number:Dimensionless Thermostat_Amb_Humidity       "Ambient Humidity [%.1f %unit%]"                          { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:ambient_humidity" }
Number:Temperature   Thermostat_Amb_Temperature    "Ambient Temperature [%.1f %unit%]"                       { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:ambient_temperature" }
String               Thermostat_Current_Eco_Mode   "Current Eco Mode [%s]"                                   { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:current_eco_mode" }
String               Thermostat_Current_Mode       "Current Mode [%s]"                                       { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:current_mode" }
Switch               Thermostat_Fan_Timer_Mode     "Fan Timer Mode"                                          { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:fan_timer_mode" }
DateTime             Thermostat_Fan_Timer_Timeout  "Fan Timer Timeout [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:fan_timer_timeout" }
String               Thermostat_HVAC_Status        "HVAC Status [%s]"                                        { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:hvac_status" }
Number:Temperature   Thermostat_Max_Temperature    "Max Temperature [%.1f %unit%]"                           { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:maximum_temperature" }
Number:Temperature   Thermostat_Min_Temperature    "Min Temperature [%.1f %unit%]"                           { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:minimum_temperature" }
Number:Temperature   Thermostat_Target_temperature "Target Temperature [%.1f %unit%]"                        { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:target_temperature" }
Number:Temperature   Thermostat_Temperature_Cool   "Temperature Cool [%.1f %unit%]"                          { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:temperature_cool" }
Number:Temperature   Thermostat_Temperature_Heat   "Temperature Heat [%.1f %unit%]"                          { channel="nest:sdm_thermostat:demo_sdm_account:living_thermostat:temperature_heat" }
```
