# Sungrow Binding

Binding for accessing the Sungrow Cloud via [Sungrow Developer Portal](https://developer-api.isolarcloud.com/).

To use this binding you have to login at [Sungrow Developer Portal](https://developer-api.isolarcloud.com/) and
create an application. With this application Sungrow will provide the data needed
to access the APIs.

You must create an Application which do **not** support "Authorize with OAuth2.0".

## Supported Things

The Binding creates a bridge for access to the API and creates a thing for each plant.

- `bridge`: Access to the APIs
- `plant`: The different Plants the user is granted.

## Thing Configuration

The Bridge need some configuration parameters which are available from the developer portal
of sungrow. The Plant does not need to be configured.

### `sample` Thing Configuration

| Name      | Type      | Description                                                                                      | Default | Required | Advanced |
|-----------|-----------|--------------------------------------------------------------------------------------------------|---------|----------|----------|
| username  | text      | E-mail address used for the Sungrow Developer Account                                            | N/A     | yes      | no       |
| password  | password  | Password used for the Sungrow Developer Account                                                  | N/A     | yes      | no       |
| appKey    | password  | Appkey; provided via Sungrow Developer Portal                                                    | N/A     | yes      | no       |
| secretKey | password  | Secret Key; provided via Sungrow Developer Portal                                                | N/A     | yes      | no       |
| region    | selection | Region - with the region you select the iSolarCloud server (EU, AUSTRALIA, CHINA, INTERNATIONAL) | N/A     | yes      | no       |
| hostname  | text      | Hostname - if available preconfigured hostname from region will be ignored                       | N/A     | no       | yes      |
| interval  | integer   | Interval in seconds the API is called                                                            | 60      | no       | yes      |

## Channels

The Channels from the Plant are genereted depending on the devices you have configured in your
plant. The Plant can be configured via normal iSolarCloud Portal or with the app.

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| control | Switch | RW         | This is the control channel |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```
### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
