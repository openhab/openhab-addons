# WebThings Binding

This binding can be used to host openHAB things as (Mozilla) [WebThings](https://iot.mozilla.org/wot/#web-thing-description) or connect them to control each other.


_Example interaction: [Link](https://imgur.com/a/axgNTpS)_

**!!! Work in progress !!!**

---

## Prerequisites

Copy the files from the ```dependencies folder``` and the binding into your addons folder

---

## Supported Things

* ***WebThings Server***: Host your openHAB things as WebThings.  


* ***WebThings Connector***: Connect openHAB things and WebThings.  
---

## Discovery

Currently no auto discovery implemented.

---

## Binding Configuration

Configuration Parameters:

1. _Server URL of WebThing Server_: Currently only Mozilla Gateways supported. This is used for by the WebThings Connector.
2. _Bearer token_: Token for the Connector Things authorization. Can be generated in the Mozilla Gateway: ```Settings --> Developer --> Create local authorization```
3. _OpenHAB IP and Port_: OpenHAB instance to get necessary information from. Used for REST API calls. 
4. _Match capabilities_: If true the WebThing Server will try to add a @type to each WebThing to match [Mozilla capabilities](https://iot.mozilla.org/schemas#capabilities) based on [default tags](https://www.openhab.org/docs/developer/bindings/thing-xml.html#default-tags) - this may not always work flawlessly for all things. If set to false, all WebThings will appear as custom things. 

---

## Thing Configuration

#### WebThings Server 

Configuration parameters:

1. _Port of WebThing Server_: Port to host your Webserver on. Multiple servers possible.
2. _Linked items_: Choose whether to create a property for every channel of the openHAB thing or for every item linked to it. For linked items naming convention of simple linking is currently advised
3. _Host all things_: Host all things (except things created by this binding) or only selected ones.
4. _Selected openHAB things_: Choose openHAB things to host as WebThings (only if 3. = false).

<br/>

#### WebThings Connector  

Configuration parameters:

1. _ID of WebThing_
2. _UID of openHAB thing_

---

## Channels


| Thing | channel  | type   | description                  |
|--------|----------|--------|------------------------------|
| WebThing Server | Port  | Number | Port of WebThing Server  |
| WebThing Server | Hosted WebThings | Text | List of openHAB things hosted as WebThings |

<br/>

| Thing | channel  | type   | description                  |
|--------|----------|--------|------------------------------|
| WebThing Connector | Update | Switch | Can be used to restart the websocket (re-connect) |
| WebThing Connector | ID  | String | ID of WebThing  |
| WebThing Connector | UID | String | UID of openHAB thing |

<br/>

---

## Example Usage cases

1. Single platform for multiple smart homes or smart home systems from one or multiple networks
2. Remote access
3. Sync openHAB instances
4. Authorization concept (expose different WebThing Servers with selected things via reverse proxy)

---

## Planned updates

* Support for more items types / properties
* Third thing which lets you choose one capability and automatically creates WebThing with needed properties, actions and events  
* Implement a way to control WebThings with "dummy" (offline) openHAB things. Currently only works one way (openHAB --> Gateway) due to offline openHAB things not being able to update their item states.
