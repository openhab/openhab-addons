# WebThings Binding

This binding can be used to:

 * Import WebThings and control them via openHAB

---

## Supported Things

* ***WebThings WebThing***: Import and control a WebThing.  

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

Additional:

 1. _Background Discovery_: If true all WebThings from the selected Gateway will be automatically discovered, without the need to manually start a search (may generate a lot of inbox entries if many things exist)
 2. _Reconnect Interval_: Time between reconnect attempts in milliseconds (ms)
 3. _Reconnect Attempts_: Number of reconnect attempts

---

## Thing Configuration

#### WebThings WebThing

Configuration parameters:

1. _Link of WebThing_: Link to WebThing to be imported
2. _Security Scheme_: Security method to access the respective WebThing
3. _Security Token_: Authorization token for "Bearer" authorization
4. _Import Token_: If true the security token can be left blank and will be imported from the binding configuration

---

## Channels

| Thing | channel  | type   | description                  |
|--------|----------|--------|------------------------------|
| WebThing | Automatic | Automatic | All channels will be automacially generated based on the WebThing properties |

---

## Known Issues

| Issue | Impact  | Status   | Workaround                  |
|--------|----------|--------|------------------------------|
| WebThings WebThing label & location can not be updated | Things need to be re-created | Active | Set during creation |

