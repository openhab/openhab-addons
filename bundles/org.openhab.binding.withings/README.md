# Withings Binding

The Withings binding uses the official public Withings data API. It integrates the following Withings product types including related measures:

- Scale
- Sleep Monitor

Measured data of the user is combined in a "Person" thing.

There is only 1 user/person available per Withings account (that is limited by the Withings data API). You can choose the user when you generate the auth-code, see Binding Configuration.

The channels are updated every 5 minutes with new data (when available). Therefore the binding causes network traffic every few minutes to synchronize the data (it transfers data from and to the official public Withings data API).

## Binding Configuration

### 1. Application Creation

Register a new Withings application here: https://account.withings.com/partner/add_oauth2

### 2. Bridge Configuration

- Create a new Withings API Thing with Paper UI
- Enter the Client-ID and Client-Secret from your registered application
  - Generate and set a new auth-code
  - Navigate to the following URL with your web browser (and replace "{CLIENT-ID}" with your Client-ID: https://account.withings.com/oauth2_user/authorize2?response_type=code&redirect_uri=https://myopenhab.org&state=statevalue&scope=user.info,user.metrics,user.activity&client_id={CLIENT-ID}
  - Following the instructions of the authorization process to grant the rights for the Withings binding
  - As soon as you are redirected to myopenhab.org take the value of the "code" query parameter which is displayed within the URL address bar of your web browser and enter it within the Auth-Code field of the bridge configuration

### 3. Discover Things

When the Withings API Thing is configured, your devices will be found automatically within Paper UI (just start a scan within the Paper UI inbox).

## Channels

### Scale Thing

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| scaleBatteryLevel   | String               | Possible values: "high", "medium" and "low"              |
| scaleLastConnection | DateTime             | Date-time of the last server connection of the device    |

### Sleep Monitor Thing

| Channel ID          | Item Type            | Description                                              |
|---------------------|----------------------|----------------------------------------------------------|
| scaleLastConnection | DateTime             | Date-time of the last server connection of the device    |

### Person Thing

| Channel ID          | Item Type            | Description                                               |
|---------------------|----------------------|-----------------------------------------------------------|
| personWeight        | Number               | Weight of the person in kg                                |
| personHeight        | Number               | Height of the person in meters                            |
| personFatMass       | Number               | Fat mass weight in kg                                     |
| lastSleepStart      | DateTime             | Start date-time of the last bedtime (unfortunately only available together with lastSleepEnd at the end of the bedtime) |
| lastSleepEnd        | DateTime             | End date-time of the last bedtime                         |
| lastSleepScore      | Number               | Last sleep score (the maximum value is 100)               |
