# WebThings Binding

This binding can be used to:

 * Import WebThings and control them via openHAB
 * Host openHAB things as (Mozilla) [WebThings](https://iot.mozilla.org/wot/#web-thing-description).
 * Connect existing openHAB things to WebThings to control each other.

_Example interaction: [Link](https://imgur.com/a/axgNTpS)_

**!!! Work in progress !!!**

---

## Prerequisites

Copy the files from the ```dependencies folder``` and the binding into your addons folder

---

## Supported Things

* ***WebThings WebThing***: Import and control a WebThing.  

* ***WebThings Server***: Host your openHAB things as WebThings.  

* ***WebThings Connector***: Connect openHAB things and WebThings.  

---

## Discovery

Automatically discovers:

* WebThings from the Gateway based on binding configuration
* WebThings from local WebThing Servers via mDNS 


---

## Binding Configuration

Configuration Parameters:

1. _Server URL of WebThing Server_: Currently only Mozilla Gateways supported. This is used for by the WebThings Connector.
2. _Bearer token_: Token for the Connector Things authorization. Can be generated in the Mozilla Gateway: ```Settings --> Developer --> Create local authorization```
3. _OpenHAB IP and Port_: OpenHAB instance to get necessary information from. Used for REST API calls. 
4. _Match capabilities_: If true the WebThing Server will try to add a @type to each WebThing to match [Mozilla capabilities](https://iot.mozilla.org/schemas#capabilities) based on [default tags](https://www.openhab.org/docs/developer/bindings/thing-xml.html#default-tags) - this may not always work flawlessly for all things. If set to false, all WebThings will appear as custom things. 


Additional:

 1. Background Discovery: If true all WebThings from the selected Gateway will be automatically discovered, without the need to manually start a search (may generate a lot of inbox entries if many things exist)
 2. Operating system: Choose your installation (can be used as fallback option to import ThingUIDs for the WebThing Server)
 3. Custom path: Set a custom path in case your operating system is not listed or you use a non-standard userdata directory

---

## Thing Configuration

#### WebThings WebThing

Configuration parameters:

1. _Link of WebThing_: Link to WebThing to be imported
2. _Security Scheme_: Security method to access the respective WebThing
3. _Security Token_: Authorization token for "Bearer" authorization
4. _Import Token_: If true the security token can be left blank and will be imported from the binding configuration

<br/>

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
| WebThing | Automatic | Automatic | All channels will be automacially generated based on the WebThing properties |


<br/>

| Thing | channel  | type   | description                  |
|--------|----------|--------|------------------------------|
| Server | Port  | Number | Port of WebThing Server  |
| Server | Hosted WebThings | Text | List of openHAB things hosted as WebThings |

<br/>

| Thing | channel  | type   | description                  |
|--------|----------|--------|------------------------------|
| Connector | Update | Switch | Can be used to restart the websocket (re-connect) |
| Connector | ID  | String | ID of WebThing  |
| Connector | UID | String | UID of openHAB thing |

<br/>

---

## Example Use Cases

1. Easily add real world objects to openHAB using any of the existing WebThing Frameworks (Node.js, Pytho, Java, Rust, C#, Go, ...) without the need for a binding
1. Single platform for multiple smart homes or smart home systems from one or multiple networks
1. Remote access
1. Sync openHAB instances
1. Authorization concept (expose different WebThing Servers with selected things via reverse proxy)

---

## Planned Updates

* Resolve known issues
* Support for more items types / properties

---

## Known Issues

| Issue | Impact  | Status   | Workaround                  |
|--------|----------|--------|------------------------------|
| Dependency incompatibility when REST API docs are installed | Binding may not start  | Active | Remove docs --> install binding --> install docs  |
| First command for switch item / OnOff property gets send but not detected by gateway | Command not properly received | Active | Send another command after initially connecting
| Add webthing as internal dependency | Dependencies need to be deployed manually | Active | Add lib directory (currently not compiling) |
| ItemStateChangeEvents for items with delimiters are not processed correctly | Some items are not controllable | Active | Switch case for special items |
| WebThings can only be accessed via their index in list (e.g. localhost:8888/0) | Removal of things from a WebThing Server will change the indices of the other things | Active | Create multiple servers so that changes to one do not affect all things |
| WebThings WebThing label & location can not be updated | Things need to be re-created | Active | Set during creation |
| ConfigOptionProvider for WebThing Server may request the openHAB thing list before the API is ready | Server may not be creatable | Resolved | Try to manually restart bundle, Re-compile without ```WebThingsConfigOptionProvider.java``` |
