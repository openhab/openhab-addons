**Installing the bundle**

Please check out the [openHAB community thread](https://community.openhab.org/t/magentatv-entertaintv-binding-for-deutsche-telekom-mr-3xx-and-4xx) and discuss your ideas, requests and technical problems with the community.

Before you install the bundle make sure that the ESH UPnP support is installed. This happens if you install a binding, which uses UPnP discovery (like the Philips HUE bundle), or manually (because the Magenta TV binding is not yet part of the standard openHAB distribution).

open OH console and run bundle:list, you should see something like

229 │ Active │ 80 │ 0.10.0.oh230 │ Eclipse SmartHome Configuration UPnP Discovery<br>
230 │ Active │ 80 │ 0.10.0.oh230 │ Eclipse SmartHome UPnP Transport Bundle

if not install upnp feature:

feature:install esh-io-transport-upnp

Next step is to copy the org.openhab.binding.entertaintv.jar file to <openHAB installation directory>/addons. After a few moments openHAB will detected and load the now bundle. It may be a good advise to restart openHAB making sure that everything is initialized properly.
