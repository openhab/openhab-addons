# Viessmann Binding

This binding connects Viessmann Devices via the new Viessmann API.
It provides features like the ViCare-App.

## Note / Important

You have to register your ViCare Account at the [Viessmann developer portal](https://developer.viessmann-climatesolutions.com/) and create a Client ID.

* `name` - `i.e. openhab`
* `Google reCAPTCHA` - `off`
* `Redirect URI` - `http://localhost:8080/viessmann/authcode/` (*)

(*) If your openHAB system is running on a different port than `8080`, you have to change this in the `Redirect URI`

### Hint: 

On the Viessmann developer portal you can add more than one RedirectURI by tapping the plus sign.

## Supported Things

The binding supports the following thing types:

* `account` - Supports connection to the Viessmann API to connect the `gateway` - thing
* `bridge` - Supports connection to the Viessmann API and connects to the first installed gateway
* `gateway` - Supports connection to the `account`- thing (Discovery)
* `device` - Provides a device which is connected (Discovery)

## Discovery

Discovery is supported for all devices connected in your account.

## Binding Configuration

The `account` thing supports the connection to the Viessmann API to connect the `gateway` - thing

* `apiKey` (required) The Client ID from the Viessmann developer portal
* `user` (required) The E-Mail address which is registered for the ViCare App
* `password` (required) The password which is registered for the ViCare App
* `apiCallLimit` (default = 1450) The limit how often call the API (*)
* `bufferApiCommands` (default = 450) The buffer for commands (*)
* `pollingInterval` (default = 0) How often the available devices should be queried in seconds (**)
* `pollingIntervalErrors` (default = 60) How often the errors should be queried in minutes
* `disablePolling` (default = OFF) Deactivates the polling to carry out the manual poll using an item

The `bridge` thing supports connection to the Viessmann API and connects to the first installed gateway

* `apiKey` (required) The Client ID from the Viessmann developer portal 
* `user` (required) The E-Mail address which is registered for the ViCare App
* `password` (required) The password which is registered for the ViCare App
* `installationId` (optional / it will be discovered) The installation ID which belongs to your installation 
* `gatewaySerial` (optional / it will be discovered) The gateway serial which belongs to your installation
* `apiCallLimit` (default = 1450) The limit how often call the API (*) 
* `bufferApiCommands` (default = 450) The buffer for commands (*)
* `pollingInterval` (default = 0) How often the available devices should be queried in seconds (**) 
* `pollingIntervalErrors` (default = 60) How often the errors should be queried in minutes 
* `disablePolling` (default = OFF) Deactivates the polling to carry out the manual poll using an item

The `gateway` thing supports connection to the `account`- thing (Discovery)

* `installationId` (optional / it will be discovered) The installation ID which belongs to your installation
* `gatewaySerial` (optional / it will be discovered) The gateway serial which belongs to your installation
* `pollingIntervalErrors` (default = 60) How often the errors should be queried in minutes
* `disablePolling` (default = OFF) Deactivates the polling to carry out the manual poll using an item


(*) Used to calculate refresh time in seconds.
(**) If set to 0, then the interval will be calculated by the binding.

## Thing Configuration

_All configurations are made in the UI_

## Channels

### `account`

| channel             | type   | RO/RW | description                                |
|---------------------|--------|-------|--------------------------------------------|
| `count-api-calls`     | Number | RO    | How often the API is called this day       |


### `bridge`

| channel             | type   | RO/RW | description                                |
|---------------------|--------|-------|--------------------------------------------|
| `count-api-calls`     | Number | RO    | How often the API is called this day       |
| `error-is-active`     | Switch | RO    | Indicates whether the error is set / unset |
| `last-error-message`  | String | RO    | Last error message from the installation   |
| `run-query-once`      | Switch | W     | Run device query once                      |
| `run-rerror-query-once` | Switch | W     | Run error query once                       |

### `gateway`

| channel             | type   | RO/RW | description                                |
|---------------------|--------|-------|--------------------------------------------|
| `error-is-active`     | Switch | RO    | Indicates whether the error is set / unset |
| `last-error-message`  | String | RO    | Last error message from the installation   |
| `run-query-once`      | Switch | W     | Run device query once                      |
| `run-rerror-query-once` | Switch | W     | Run error query once                       |

### `device`

There are many different channels.
The channels are automatically generated for all available features.

## Breaking changes

### Version 5.1.0

* New `account` and `gateway` thing have been added to support gateway selection. 
  The existing `device` thing can be manually switched to the new `gateway` thing as bridge if needed.

### Version 2.3.10

All channels on `device` - thing needs to be recreated to support Units Of Measurement.
This happens automatically.

The item type of each item has to be adjusted:

| unit              | old item type | new item type         |
|-------------------|---------------|-----------------------|
| hour, minutes,... | Number        | Number:Time           |
| percent           | Number        | Number:Dimensionless  |
| temperature       | Number        | Number:Temperature    |
