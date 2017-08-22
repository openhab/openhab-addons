# Nest Binding

[Nest Labs](https://nest.com/) developed/acquired the Wi-Fi enabled Nest Learning Thermostat, the Nest Protect Smoke+CO detector, and the Nest Cam.  These devices are supported by this binding, which communicates with the Nest API over a secure, RESTful API to Nest's servers. Monitoring ambient temperature and humidity, changing HVAC mode, changing heat or cool setpoints, monitoring and changing your "home/away" status, and monitoring your Nest Protects and Nest Cams can be accomplished through this binding.

## Prerequisites

In order to use this binding, you will have to register as a [Nest Developer](https://developer.nest.com/) and [register a new Product](https://developer.nest.com/products/new) (free and instant).

> Make sure to grant [all the permissions](https://developers.nest.com/documentation/cloud/permissions-overview#available-permissions) you intend to use.  **When in doubt, enable the permission,** because you will otherwise have to reauthorize the binding if you later have to change the permissions.  

Leave the **Redirect URI** field **blank** for PIN-based authorization. At this point, you will have your `client_id` (**Product ID**) and `client_secret` (**Product Secret**).

Once you've created your [product](https://developer.nest.com/products) as above, paste the **Authorization URL** into a new tab in your browser.  This will have you login to your normal Nest account (if not already logged in), and will then present the PIN.  Prepare to copy and paste your values for `client_id`, `client_secret` and `pin_code` in order to configure the binding.

## Binding Configuration

To configure the binding you will add a thing in the UX, adding the nest api connection bridge thing.  This thing will have configuraton options set in it which you will fill in
from the previous steps.  The client_id, client_secret and pincode.  There is also an access_token in the advanced section which is what you will actually use to talk to nest, the pincode is converted into this.

## Known Issues

1. The binding initiates outbound TCP connections to the Nest infrastructure on port 9553 (however, which outbound ports are used is determined dynamically by the Nest cloud service and may be different from 9553). If the log shows "Connection timed out" or "Exception reading from Nest: Could not get data model", ensure that outbound connections are not being blocked by a firewall.
2. The Nest API rounds humidity to 5%, degrees Fahrenheit to whole degrees, and degrees Celsius to 0.5 degrees, so your Nest app will likely show slightly different values from what is available from the API.
3. The binding only uses celsius for all it's temperature numbers.

## Attribution

This doc is written by John Cocula and copied from the 1.0 binding for nest.
