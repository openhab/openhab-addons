# Nest Binding

The Nest binding integrates devices by [Nest](https://store.google.com/us/category/connected_home?) using the [Smart Device Management](https://developers.google.com/nest/device-access/api) (SDM) API and the Works with Nest (WWN) API.

To be able to use the SDM API it is required to first [register](https://developers.google.com/nest/device-access/registration) and pay a US$5 non-refundable registration fee.

It is also possible to use the older WWN API with this binding.
For this you need to have the account details of a previously registered WWN API account.
Another requirement is that you have not yet migrated your Nest account to a Google account (which is irreversible).
It is no longer possible to register new WWN API accounts because the WWN API runs in maintenance mode.
See also [What's happening at Nest?](https://nest.com/whats-happening/).

Because the SDM and WWN APIs run on servers in the cloud, a connection with the Internet is required for sending and receiving information.
The binding uses HTTPS to connect to the APIs using port 443.
When using the WWN API, the binding also connects to servers on port 9553.
So make sure outbound connections to these ports are not blocked by a firewall.

## Supported Things

The table below lists the Nest binding thing types:

| Things                                  | Description                                                                                                                                    | SDM Thing Type | WWN Thing Type     |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|----------------|--------------------|
| Nest Account (SDM, WWN)                 | An account for using the Nest (SDM/WWN) REST API                                                                                               | sdm_account    | wwn_account        |
| Nest Cam (Indoor, IQ, Outdoor), Dropcam | A Nest Cam registered with your account                                                                                                        | sdm_camera     | wwn_camera         |
| Nest Hello Doorbell                     | A Nest Doorbell registered with your account                                                                                                   | sdm_doorbell   | wwn_camera         |
| Nest Hub (Max)                          | A Nest Display registered with your account                                                                                                    | sdm_display    | wwn_camera         |
| Nest Protect                            | The smoke detector/Nest Protect for the account                                                                                                |                | wwn_smoke_detector |
| Nest Thermostat (E)                     | A Thermostat to control the various aspects of the house's HVAC system                                                                         | sdm_thermostat | wwn_thermostat     |
| Structure                               | The Nest structure defines the house the account has setup on Nest. You will only have more than one structure if you have more than one house |                | wwn_structure      |

The SDM API currently does not support Nest Protect devices.
There are no structure Things when using the SDM API, because the SDM API does not support setting the Home/Away status like the WWN API does.

To use one of the Nest APIs, add the corresponding Account Thing using the UI and configure the required parameters.
After configuring an Account Thing, you can use it to discover the connected devices which are then added the Inbox.

## SDM Account Configuration

### Google Account Requirement

To be able to use the SDM API it is required that you use a Google Account with your Nest devices.
If you still use the WWN API, you can no longer use the WWN API after migrating to a Google Account.
So if you have not yet migrated your account, check that all the functionality you require is provided by the SDM API and SDM Things in the binding.
Most notably, there is no support for the Nest Protect in the SDM API and you cannot change your Home/Away status.
To migrate to a Google account, follow the migration steps in the [Nest accounts FAQ](https://support.google.com/googlenest/answer/9297676?co=GENIE.Platform%3DiOS&hl=en&oco=0#accountmigration&accountmigration1&#accountmigration2&#accountmigration3&zippy=%2Chow-do-i-migrate-my-account)

### SDM Configuration Parameters

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
    1. Add "https://www.google.com" to the "Authorized redirect URIs"
    1. Click "Create" to create the client
    1. Copy and save the generated **Client ID** (e.g. `1046297811237-3f5sj4ccfubit0fum027ral82jgffsd1.apps.googleusercontent.com`) and **Client Secret** (e.g. `726kcU-d1W4RXxEJA79oZ0oG`) somewhere
1. Configure the SDM project to use the created client:
    1. Go the the SDM [Projects](https://console.nest.google.com/device-access/project-list) page
    1. Click on your SDM Project to show its details
    1. Scroll to "Project Info > OAuth client ID" and open the options menu (3 stacked dots) at the end of the line
    1. Select the "Edit" option
    1. Copy/paste the saved OAuth 2.0 Client ID here (e.g. `1046297811237-3f5sj4ccfubit0fum027ral82jgffsd1.apps.googleusercontent.com`)
    1. Click the "Save" button at the end of the line to update the project

Finally, an SDM Account Thing can be created to access the SDM project using the SDM API with the created client:

1. Create a new "Nest SDM Account" Thing in openHAB
1. Copy/paste the saved SDM **Project ID** to SDM group parameter in the SDM Account Thing configuration parameters (e.g. `585de72e-968c-435c-b16a-31d1d3f76833`)
1. Copy/paste the saved OAuth 2.0 **Client ID** to SDM group parameter (e.g. `1046297811237-3f5sj4ccfubit0fum027ral82jgffsd1.apps.googleusercontent.com`)
1. Copy/paste the saved OAuth 2.0 **Client Secret** to SDM group parameter (e.g. `726kcU-d1W4RXxEJA79oZ0oG`)
1. Create an authorization code for the binding:
    1. Replace the **Project ID** and **Client ID** in the URL below with your SDM Project ID and SDM OAuth 2.0 Client ID and open the URL in a new browser tab:
       
       `https://nestservices.google.com/partnerconnections/<ProjectID>/auth?scope=https://www.googleapis.com/auth/sdm.service&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<ClientID>`
       
       For the example values used so far this is:
       
       `https://nestservices.google.com/partnerconnections/585de72e-968c-435c-b16a-31d1d3f76833/auth?scope=https://www.googleapis.com/auth/sdm.service&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=1046297811237-3f5sj4ccfubit0fum027ral82jgffsd1.apps.googleusercontent.com`
    1. Enable all the permissions you want to use with the binding and click "Next" to continue
    1. Login using your Google account when prompted
    1. On the "Google hasn't verified this app" page, click on "Advanced"
    1. Then click on "Go to ... (advanced)"
    1. Now "Allow" the SDM permissions and confirm your choices again by clicking "Allow"
    1. After your browser has been redirected to https://www.google.com, the **Authorization Code** will be set in the browser URL as value of the "code" URL query parameter
    1. Copy/paste the **Authorization Code** to the SDM group parameter in the openHAB Nest SDM Account Thing configuration
1. All required SDM Account Thing configuration parameters have now been entered so create it by clicking "Create Thing"

The SDM Account Thing should now be ONLINE and have as status description "Using periodic refresh".
It should also be possible to use the configured account to discover your Nest devices via the Inbox.

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

Lookup your Google Cloud Platform (GCP) Project ID:

1. Open the [IAM & Admin > Settings](https://console.cloud.google.com/iam-admin/settings)
1. Copy and save the GCP **Project ID** (e.g. `openhab-12345`)

Next an OAuth 2.0 client is created which is used to create a Pub/Sub subscription for listening to SDM events by the binding:

1. Open the "Credentials" page ([APIs & Services > Credentials](https://console.cloud.google.com/apis/credentials)):
1. Click the "Create Credentials" button at the top of the page
1. Choose "OAuth client ID"
1. As "Application type" choose "Web application"
1. Give it a name so you can remember what it is used for (e.g. `Nest Binding Pub/Sub`)
1. Add "https://www.google.com" to the "Authorized redirect URIs"
1. Click "Create" to create the client
1. Copy and save the generated **Client ID** (e.g. `1046297811237-lg27h26kln6r1nbg54jpg6nfjg6h4b3n.apps.googleusercontent.com`) and **Client Secret** (e.g. `1-k78-XcHhp_gdZF-I6JaIHp`) somewhere

Finally, the existing SDM Account Thing can be updated so it can subscribe to SDM events:

1. Open the configuration details of your existing "Nest SDM Account" Thing in openHAB
1. Copy/paste the saved GCP **Project ID** to Pub/Sub group parameter (e.g. `openhab-123`)
1. Enter a name in **Subscription ID** that uniquely identifies the Pub/Sub subscription used by the binding
   
   > Must be 3-255 characters, start with a letter, and contain only the following characters: letters, numbers, dashes (-), periods (.), underscores (_), tildes (~), percents (%) or plus signs (+). Cannot start with  goog.
1. Copy/paste the saved OAuth 2.0 **Client ID** to Pub/Sub group parameter (e.g. `1046297811237-lg27h26kln6r1nbg54jpg6nfjg6h4b3n.apps.googleusercontent.com`)
1. Copy/paste the saved OAuth 2.0 **Client Secret** to Pub/Sub group parameter (e.g. `1-k78-XcHhp_gdZF-I6JaIHp`)
1. Create an authorization code for the binding:
    1. Replace the **Client ID** in the URL below with your Pub/Sub OAuth 2.0 Client ID and open the URL in a new browser tab:
       
       `https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/pubsub&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=<ClientID>`
       
       For the example client this is:
       
       `https://accounts.google.com/o/oauth2/v2/auth?scope=https://www.googleapis.com/auth/pubsub&access_type=offline&prompt=consent&include_granted_scopes=true&response_type=code&redirect_uri=https://www.google.com&client_id=1046297811237-lg27h26kln6r1nbg54jpg6nfjg6h4b3n.apps.googleusercontent.com`
    1. Login using your Google account when prompted
    1. On the "Google hasn't verified this app" page, click on "Advanced"
    1. Then click on "Go to ... (advanced)"
    1. Now "Allow" the Pub/Sub permissions and confirm your choices again by clicking "Allow"
    1. After your browser has been redirected to https://www.google.com, the **Authorization Code** will be set in the browser URL as value of the "code" URL query parameter
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

## WWN Account Configuration

To configure the binding to use the WWN API, add a new "Nest WWN Account" Thing in the UI and enter the **Product ID**, **Product Secret** and **Access Token** of an existing WWN account as configuration parameters.
It is no longer possible to register new WWN accounts with Nest because the WWN API runs in maintenance mode.

## Discovery

The binding will discover all Nest Things from your account when you add and configure a Nest SDM or WWN Account Thing.

## Channels

### SDM/WWN Account Channels

The account Thing Types do not have any channels.

### SDM Camera/Display/Doorbell Channels

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

### SDM Thermostat Channels

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

### WWN Camera Channels

**Camera group channels**

Information about the camera.

| Channel Type ID       | Item Type | Description                                       | Read Write |
|-----------------------|-----------|---------------------------------------------------|:----------:|
| app_url               | String    | The app URL to see the camera                     |      R     |
| audio_input_enabled   | Switch    | If the audio input is currently enabled           |      R     |
| last_online_change    | DateTime  | Timestamp of the last online status change        |      R     |
| public_share_enabled  | Switch    | If public sharing is currently enabled            |      R     |
| public_share_url      | String    | The URL to see the public share of the camera     |      R     |
| snapshot_url          | String    | The URL to use for a snapshot of the video stream |      R     |
| streaming             | Switch    | If the camera is currently streaming              |     R/W    |
| video_history_enabled | Switch    | If the video history is currently enabled         |      R     |
| web_url               | String    | The web URL to see the camera                     |      R     |

**Last event group channels**

Information about the last camera event (requires Nest Aware subscription).

| Channel Type ID    | Item Type | Description                                                                        | Read Write |
|--------------------|-----------|------------------------------------------------------------------------------------|:----------:|
| activity_zones     | String    | Identifiers for activity zones that detected the event (comma separated)           |      R     |
| animated_image_url | String    | The URL showing an animated image for the camera event                             |      R     |
| app_url            | String    | The app URL for the camera event, allows you to see the camera event in an app     |      R     |
| end_time           | DateTime  | Timestamp when the camera event ended                                              |      R     |
| has_motion         | Switch    | If motion was detected in the camera event                                         |      R     |
| has_person         | Switch    | If a person was detected in the camera event                                       |      R     |
| has_sound          | Switch    | If sound was detected in the camera event                                          |      R     |
| image_url          | String    | The URL showing an image for the camera event                                      |      R     |
| start_time         | DateTime  | Timestamp when the camera event started                                            |      R     |
| urls_expire_time   | DateTime  | Timestamp when the camera event URLs expire                                        |      R     |
| web_url            | String    | The web URL for the camera event, allows you to see the camera event in a web page |      R     |

### WWN Smoke Detector Channels

| Channel Type ID       | Item Type | Description                                                                       | Read Write |
|-----------------------|-----------|-----------------------------------------------------------------------------------|:----------:|
| co_alarm_state        | String    | The carbon monoxide alarm state of the Nest Protect (OK, EMERGENCY, WARNING)      |      R     |
| last_connection       | DateTime  | Timestamp of the last successful interaction with Nest                            |      R     |
| last_manual_test_time | DateTime  | Timestamp of the last successful manual test                                      |      R     |
| low_battery           | Switch    | Reports whether the battery of the Nest protect is low (if it is battery powered) |      R     |
| manual_test_active    | Switch    | Manual test active at the moment                                                  |      R     |
| smoke_alarm_state     | String    | The smoke alarm state of the Nest Protect (OK, EMERGENCY, WARNING)                |      R     |
| ui_color_state        | String    | The current color of the ring on the smoke detector (GRAY, GREEN, YELLOW, RED)    |      R     |

### WWN Structure Channels

| Channel Type ID              | Item Type | Description                                                                                            | Read Write |
|------------------------------|-----------|--------------------------------------------------------------------------------------------------------|:----------:|
| away                         | String    | Away state of the structure (HOME, AWAY)                                                               |     R/W    |
| country_code                 | String    | Country code of the structure ([ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)) |      R     |
| co_alarm_state               | String    | Carbon Monoxide alarm state (OK, EMERGENCY, WARNING)                                                   |      R     |
| eta_begin                    | DateTime  | Estimated time of arrival at home, will setup the heat to turn on and be warm                          |      R     |
| peak_period_end_time         | DateTime  | Peak period end for the Rush Hour Rewards program                                                      |      R     |
| peak_period_start_time       | DateTime  | Peak period start for the Rush Hour Rewards program                                                    |      R     |
| postal_code                  | String    | Postal code of the structure                                                                           |      R     |
| rush_hour_rewards_enrollment | Switch    | If rush hour rewards system is enabled or not                                                          |      R     |
| security_state               | String    | Security state of the structure (OK, DETER)                                                            |      R     |
| smoke_alarm_state            | String    | Smoke alarm state (OK, EMERGENCY, WARNING)                                                             |      R     |
| time_zone                    | String    | The time zone for the structure ([IANA time zone format](https://www.iana.org/time-zones))             |      R     |

### WWN Thermostat Channels

| Channel Type ID             | Item Type            | Description                                                                            | Read Write |
|-----------------------------|----------------------|----------------------------------------------------------------------------------------|:----------:|
| can_cool                    | Switch               | If the thermostat can actually turn on cooling                                         |      R     |
| can_heat                    | Switch               | If the thermostat can actually turn on heating                                         |      R     |
| eco_max_set_point           | Number:Temperature   | The eco range max set point temperature                                                |      R     |
| eco_min_set_point           | Number:Temperature   | The eco range min set point temperature                                                |      R     |
| fan_timer_active            | Switch               | If the fan timer is engaged                                                            |     R/W    |
| fan_timer_duration          | Number:Time          | Length of time that the fan is set to run (15, 30, 45, 60, 120, 240, 480, 960 minutes) |     R/W    |
| fan_timer_timeout           | DateTime             | Timestamp when the fan stops running                                                   |      R     |
| has_fan                     | Switch               | If the thermostat can control the fan                                                  |      R     |
| has_leaf                    | Switch               | If the thermostat is currently in a leaf mode                                          |      R     |
| humidity                    | Number:Dimensionless | Indicates the current relative humidity                                                |      R     |
| last_connection             | DateTime             | Timestamp of the last successful interaction with Nest                                 |      R     |
| locked                      | Switch               | If the thermostat has the temperature locked to only be within a set range             |      R     |
| locked_max_set_point        | Number:Temperature   | The locked range max set point temperature                                             |      R     |
| locked_min_set_point        | Number:Temperature   | The locked range min set point temperature                                             |      R     |
| max_set_point               | Number:Temperature   | The max set point temperature                                                          |     R/W    |
| min_set_point               | Number:Temperature   | The min set point temperature                                                          |     R/W    |
| mode                        | String               | Current mode of the Nest thermostat (HEAT, COOL, HEAT_COOL, ECO, OFF)                  |     R/W    |
| previous_mode               | String               | The previous mode of the Nest thermostat (HEAT, COOL, HEAT_COOL, ECO, OFF)             |      R     |
| state                       | String               | The active state of the Nest thermostat (HEATING, COOLING, OFF)                        |      R     |
| temperature                 | Number:Temperature   | Current temperature                                                                    |      R     |
| time_to_target              | Number:Time          | Time left to the target temperature approximately                                      |      R     |
| set_point                   | Number:Temperature   | The set point temperature                                                              |     R/W    |
| sunlight_correction_active  | Switch               | If sunlight correction is active                                                       |      R     |
| sunlight_correction_enabled | Switch               | If sunlight correction is enabled                                                      |      R     |
| using_emergency_heat        | Switch               | If the system is currently using emergency heat                                        |      R     |

Note that the Nest API rounds Thermostat values so they will differ from what shows up in the Nest App.
The Nest API applies the following rounding:

*   degrees Celsius to 0.5 degrees
*   degrees Fahrenheit to whole degrees
*   humidity to 5%

## Example

You can use the discovery functionality of the binding to obtain the deviceId and structureId values for defining Nest things in files.

### sdm-demo.things

```
Bridge nest:sdm_account:demo_sdm_account [ sdmProjectId="585de72e-968c-435c-b16a-31d1d3f76833", sdmClientId="1046297811237-3f5sj4ccfubit0fum027ral82jgffsd1.apps.googleusercontent.com", sdmClientSecret="726kcU-d1W4RXxEJA79oZ0oG", sdmAuthorizationCode="xkkY3qYtfZCzaXCcPxpOELUW8EhgiSMD3n9jmzJ3m0yerkQpVRdj5vqWRjMSIG", pubsubProjectId="openhab-12345", pubsubSubscriptionId="nest-sdm-events", pubsubClientId="1046297811237-lg27h26kln6r1nbg54jpg6nfjg6h4b3n.apps.googleusercontent.com", pubsubClientSecret="1-k78-XcHhp_gdZF-I6JaIHp", pubsubAuthorizationCode="tASfQq7gn6sfbUSbwRufbMI0BYDzh1d7MBG2G7vdZpbhjmZfwDp5MkeaX0iMxn" ] {
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

```
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

### wwn-demo.things

```
Bridge nest:wwn_account:demo_wwn_account [ productId="8fdf9885-ca07-4252-1aa3-f3d5ca9589e0", productSecret="QITLR3iyUlWaj9dbvCxsCKp4f", accessToken="c.6rse1xtRk2UANErcY0XazaqPHgbvSSB6owOrbZrZ6IXrmqhsr9QTmcfaiLX1l0ULvlI5xLp01xmKeiojHqozLQbNM8yfITj1LSdK28zsUft1aKKH2mDlOeoqZKBdVIsxyZk4orH0AvKEZ5aY" ] {
    Thing wwn_camera         fish_cam           [ deviceId="qw0NNE8ruxA9AGJkTaFH3KeUiJaONWKiH9Gh3RwwhHClonIexTtufQ" ]
    Thing wwn_smoke_detector hallway_smoke      [ deviceId="Tzvibaa3lLKnHpvpi9OQeCI_z5rfkBAV" ]
    Thing wwn_structure      home               [ structureId="20wKjydArmMV3kOluTA7JRcZg8HKBzTR-G_2nRXuIN1Bd6laGLOJQw" ]
    Thing wwn_thermostat     living_thermostat  [ deviceId="ZqAKzSv6TO6PjBnOCXf9LSI_z5rfkBAV" ]
}
```

### wwn-demo.items

```
/* WWN Camera */
String   Cam_App_URL               "App URL [%s]"                                                      { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#app_url" }
Switch   Cam_Audio_Input_Enabled   "Audio Input Enabled"                                               { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#audio_input_enabled" }
DateTime Cam_Last_Online_Change    "Last Online Change [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"          { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#last_online_change" }
String   Cam_Snapshot_URL          "Snapshot URL [%s]"                                                 { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#snapshot_url" }
Switch   Cam_Streaming             "Streaming"                                                         { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#streaming" }
Switch   Cam_Public_Share_Enabled  "Public Share Enabled"                                              { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#public_share_enabled" }
String   Cam_Public_Share_URL      "Public Share URL [%s]"                                             { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#public_share_url" }
Switch   Cam_Video_History_Enabled "Video History Enabled"                                             { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#video_history_enabled" }
String   Cam_Web_URL               "Web URL [%s]"                                                      { channel="nest:wwn_camera:demo_wwn_account:fish_cam:camera#web_url" }
String   Cam_LE_Activity_Zones     "Last Event Activity Zones [%s]"                                    { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#activity_zones" }
String   Cam_LE_Animated_Image_URL "Last Event Animated Image URL [%s]"                                { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#animated_image_url" }
String   Cam_LE_App_URL            "Last Event App URL [%s]"                                           { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#app_url" }
DateTime Cam_LE_End_Time           "Last Event End Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"         { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#end_time" }
Switch   Cam_LE_Has_Motion         "Last Event Has Motion"                                             { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#has_motion" }
Switch   Cam_LE_Has_Person         "Last Event Has Person"                                             { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#has_person" }
Switch   Cam_LE_Has_Sound          "Last Event Has Sound"                                              { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#has_sound" }
String   Cam_LE_Image_URL          "Last Event Image URL [%s]"                                         { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#image_url" }
DateTime Cam_LE_Start_Time         "Last Event Start Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"       { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#start_time" }
DateTime Cam_LE_URLs_Expire_Time   "Last Event URLs Expire Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#urls_expire_time" }
String   Cam_LE_Web_URL            "Last Event Web URL [%s]"                                           { channel="nest:wwn_camera:demo_wwn_account:fish_cam:last_event#web_url" }

/* WWN Smoke Detector */
String   Smoke_CO_Alarm            "CO Alarm [%s]"                                            { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:co_alarm_state" }
Switch   Smoke_Battery_Low         "Battery Low"                                              { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:low_battery" }
Switch   Smoke_Manual_Test         "Manual Test"                                              { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:manual_test_active" }
DateTime Smoke_Last_Connection     "Last Connection [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"    { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:last_connection" }
DateTime Smoke_Last_Manual_Test    "Last Manual Test [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"   { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:last_manual_test_time" }
String   Smoke_Smoke_Alarm         "Smoke Alarm [%s]"                                         { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:smoke_alarm_state" }
String   Smoke_UI_Color            "UI Color [%s]"                                            { channel="nest:wwn_smoke_detector:demo_wwn_account:hallway_smoke:ui_color_state" }

/* WWN Thermostat */
Switch   Thermostat_Can_Cool       "Can Cool"                                                 { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:can_cool" }
Switch   Thermostat_Can_Heat       "Can Heat"                                                 { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:can_heat" }
Number:Temperature Therm_EMaxSP    "Eco Max Set Point [%.1f %unit%]"                          { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:eco_max_set_point" }
Number:Temperature Therm_EMinSP    "Eco Min Set Point [%.1f %unit%]"                          { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:eco_min_set_point" }
Switch   Thermostat_FT_Active      "Fan Timer Active"                                         { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:fan_timer_active" }
Number:Time Thermostat_FT_Duration "Fan Timer Duration [%d %unit%]"                           { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:fan_timer_duration" }
DateTime Thermostat_FT_Timeout     "Fan Timer Timeout [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"  { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:fan_timer_timeout" }
Switch   Thermostat_Has_Fan        "Has Fan"                                                  { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:has_fan" }
Switch   Thermostat_Has_Leaf       "Has Leaf"                                                 { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:has_leaf" }
Number:Dimensionless Therm_Hum     "Humidity [%.1f %unit%]"                                   { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:humidity" }
DateTime Thermostat_Last_Conn      "Last Connection [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"    { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:last_connection" }
Switch   Thermostat_Locked         "Locked"                                                   { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:locked" }
Number:Temperature Therm_LMaxSP    "Locked Max Set Point [%.1f %unit%]"                       { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:locked_max_set_point" }
Number:Temperature Therm_LMinSP    "Locked Min Set Point [%.1f %unit%]"                       { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:locked_min_set_point" }
Number:Temperature Therm_Max_SP    "Max Set Point [%.1f %unit%]"                              { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:max_set_point" }
Number:Temperature Therm_Min_SP    "Min Set Point [%.1f %unit%]"                              { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:min_set_point" }
String   Thermostat_Mode           "Mode [%s]"                                                { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:mode" }
String   Thermostat_Previous_Mode  "Previous Mode [%s]"                                       { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:previous_mode" }
String   Thermostat_State          "State [%s]"                                               { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:state" }
Number:Temperature Thermostat_SP   "Set Point [%.1f %unit%]"                                  { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:set_point" }
Switch   Thermostat_Sunlight_CA    "Sunlight Correction Active"                               { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:sunlight_correction_active" }
Switch   Thermostat_Sunlight_CE    "Sunlight Correction Enabled"                              { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:sunlight_correction_enabled" }
Number:Temperature Therm_Temp      "Temperature [%.1f %unit%]"                                { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:temperature" }
Number:Time Therm_Time_To_Target   "Time To Target [%d %unit%]"                               { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:time_to_target" }
Switch   Thermostat_Using_Em_Heat  "Using Emergency Heat"                                     { channel="nest:wwn_thermostat:demo_wwn_account:living_thermostat:using_emergency_heat" }

/* WWN Structure */
String   Home_Away                 "Away [%s]"                                                { channel="nest:wwn_structure:demo_wwn_account:home:away" }
String   Home_Country_Code         "Country Code [%s]"                                        { channel="nest:wwn_structure:demo_wwn_account:home:country_code" }
String   Home_CO_Alarm_State       "CO Alarm State [%s]"                                      { channel="nest:wwn_structure:demo_wwn_account:home:co_alarm_state" }
DateTime Home_ETA                  "ETA [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"                { channel="nest:wwn_structure:demo_wwn_account:home:eta_begin" }
DateTime Home_PP_End_Time          "PP End Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"        { channel="nest:wwn_structure:demo_wwn_account:home:peak_period_end_time" }
DateTime Home_PP_Start_Time        "PP Start Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]"      { channel="nest:wwn_structure:demo_wwn_account:home:peak_period_start_time" }
String   Home_Postal_Code          "Postal Code [%s]"                                         { channel="nest:wwn_structure:demo_wwn_account:home:postal_code" }
Switch   Home_Rush_Hour_Rewards    "Rush Hour Rewards"                                        { channel="nest:wwn_structure:demo_wwn_account:home:rush_hour_rewards_enrollment" }
String   Home_Security_State       "Security State [%s]"                                      { channel="nest:wwn_structure:demo_wwn_account:home:security_state" }
String   Home_Smoke_Alarm_State    "Smoke Alarm State [%s]"                                   { channel="nest:wwn_structure:demo_wwn_account:home:smoke_alarm_state" }
String   Home_Time_Zone            "Time Zone [%s]"                                           { channel="nest:wwn_structure:demo_wwn_account:home:time_zone" }
```

## Attribution

This documentation contains parts written by John Cocula which were copied from the 1.0 Nest binding.
