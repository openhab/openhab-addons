# openHAB Cloud Connector

The openHAB Cloud Connector allows connecting the local openHAB runtime to a remote [openHAB Cloud](https://github.com/openhab/openhab-cloud/blob/master/README.md) instance, such as [myopenHAB.org](http://www.myopenHAB.org), which is an instance of the
openHAB Cloud service hosted by the [openHAB Foundation](http://www.openhabfoundation.org/).

## Features

The openHAB Cloud service (and thus the connector to it) is useful for different use cases:

* It allows remote access to local openHAB instances without having to expose ports to the Internet or to require a complex VPN setup.
* It serves as a connector to Google Cloud Messaging (GCM) and Apple Push Notifications (APN) for pushing notifications to mobile phone apps.
* It brings integration possibilities with services that require an OAuth2 authentication against a web server, such as IFTTT or Amazon Alexa Skills.

## UUID and Secret

To authenticate with the openHAB Cloud your local openHAB runtime generates two values, which need to be entered in your account settings of the openHAB Cloud service. The first one is a unique identifier, which allows to identify your runtime. One can think of it as something similar like a username for the cloud authentication. The second one is a random secret key which serves as a password. Both values are written to the local file system. If you loose these files for some reason, openHAB will automatically generates new ones. You will then have to reconfigure UUID and secret in the openHAB Cloud service under the _My account_ section.

Location of UUID and Secret:

|File | regular Installation | APT Installation |
|-----|----------------------|------------------|
|UUID | userdata/uuid        | /var/lib/openhab2/uuid |
|Secret | userdata/openhabcloud/secret | /var/lib/openhab2/openhabcloud/secret |

## Configuration

After installing this add-on, you will find configuration options in the Paper UI under _Configuration->Services->IO->openHAB Cloud_:

![Configuration](doc/cfg.png)

Alternatively, you can configure the settings in the file `conf/services/openhabcloud.cfg`:

```
############################## openHAB Cloud Connector #############################

# The URL of the openHAB Cloud service to connect to.
# Optional, default is set to the service offered by the openHAB Foundation
# (https://myopenhab.org/)
#baseURL=

# Defines the mode in which you want to operate the connector.
# Possible values are:
# - notification: Only push notifications are enabled, no remote access is allowed.
# - remote: Push notifications and remote access are enabled.
# Optional, default is 'remote'.
#mode=

# A comma-separated list of items to be exposed to external services like IFTTT. 
# Events of those items are pushed to the openHAB Cloud and commands received for
# these items from the openHAB Cloud service are accepted and sent to the local bus.
# Optional, default is an empty list.
#expose=
```

