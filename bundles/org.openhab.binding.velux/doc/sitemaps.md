
# Velux Sitemap Example

```perl
sitemap velux label="Velux Environment"
{
    Frame label="Velux Shutter and Window" {

        Switch  item=V_DG_M_W_OPEN
        Switch  item=V_DG_M_W_UNLOCKED
        Switch  item=V_DG_M_W_CLOSED
        Slider  item=V_DG_M_W
    }
    
    Frame label="Velux Bridge Status" {
        Text    item=V_BRIDGE_STATUS
        Text    item=V_BRIDGE_TIMESTAMP
        Switch  item=V_BRIDGE_RELOAD
    }

    Frame label="Velux Bridge Status" {
        Switch  item=V_BRIDGE_DETECTION  
        Text    item=V_BRIDGE_CHECK
        Text    item=V_BRIDGE_SCENES
        Text    item=V_BRIDGE_PRODUCTS
    }

    Frame label="Velux Bridge Configuration" {
        Text    item=V_BRIDGE_FIRMWARE
        Text    item=V_BRIDGE_IPADDRESS
        Text    item=V_BRIDGE_SUBNETMASK
        Text    item=V_BRIDGE_DEFAULTGW
        Switch  item=V_BRIDGE_DHCP
        Text    item=V_BRIDGE_WLANSSID
        Text    item=V_BRIDGE_WLANPASSWD    
    }
    
}
```
