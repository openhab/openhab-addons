# Freebox Binding Configuration

This binding integrates the [Freebox Revolution](http://www.free.fr/adsl/freebox-revolution.html) to your openHab installation.


##Binding configuration

The binding will use the default address used by Free to access your Freebox Server (mafreebox.freebox.fr).

## Authentication

You'll have to authorise openHAB to connect to your Freebox. Here is the process described :

**Step 1** At binding startup, if no token is recorded in the Item configuration, the following message
will be displayed in the OSGi console :

```
            ####################################################################
            # Please accept activation request directly on your freebox        #
            # Once done, record Apptoken in the Freebox Item configuration     #
            # bEK7a7O8GkxxxxxxxxxxXBsKu/xxxttttwj5bXSssd5gUvSXs4vrpuhZwelEo804 #
            ####################################################################
```

**Step 2** Run to your Freebox and approve the pairing request for openHAB Freebox Binding that is displayed on the Freebox screen

**Step 3** Record the apptoken in the Freebox Item configuration

**Optionally** you can log in your Freebox admin console to allocate needed rights to openhab

Once initialized, the Item will generate all available chanels.

##Channels

* fwversion
* uptime
* restarted
* tempcpum
* tempcpub
* tempswitch
* fanspeed
* reboot
* lcd_brightness
* lcd_orientation
* lcd_forced
* wifi_status
* xdsl_status
* line_status
* ipv4
* rate_up
* rate_down
* bytes_up
* bytes_down
* onhook
* ringing
* call_number
* call_duration
* call_timestamp
* call_status
* call_name


