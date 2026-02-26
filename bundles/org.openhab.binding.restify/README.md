# RESTify Binding

The RESTify binding exposes configurable HTTP endpoints from openHAB.
Each endpoint returns JSON built from a declarative JSON script.
Scripts can return static values, `$item` values, `$thing` values, nested objects, and arrays.

## Supported Things

This binding currently provides one thing type.

- `restify:endpoint`
  Creates one HTTP endpoint under `/restify/<path>`.

## Discovery

This binding does not provide auto-discovery.
Create endpoint things manually.

## Binding Configuration

The binding has one optional parameter.

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `enforceAuthentication` | boolean | `false` | If `true`, every endpoint must define authorization in its endpoint JSON config. |
| `defaultBasic` | text | empty | Default Basic credentials in `username:password` format, used when endpoint authorization is not configured and the request has a `Basic` authorization prefix. |
| `defaultBearer` | text | empty | Default Bearer token, used when endpoint authorization is not configured and the request has a `Bearer` authorization prefix. |

## Thing Configuration

Thing type: `restify:endpoint`.

| Name | Type | Required | Advanced | Description |
|------|------|----------|----------|-------------|
| `path` | text | yes | no | URL path for the endpoint. Must start with `/`. `/` alone is not allowed. |
| `method` | enum (`GET`,`POST`,`PUT`,`DELETE`) | yes | yes | HTTP method accepted by this endpoint. |
| `endpoint` | text (script context) | yes | no | JSON script describing authorization and response schema. |

## Endpoint Script Format

The `endpoint` config must be a JSON object with this shape.

```json
{
  "authorization": {
    "type": "Basic",
    "username": "api",
    "password": "secret"
  },
  "response": {
    "message": "Hello World"
  }
}
```

`authorization` is optional.
`response` is required and must be a JSON object.

Supported authorization formats:

- Basic:
```json
{ "type": "Basic", "username": "api", "password": "secret" }
```
- Bearer:
```json
{ "type": "Bearer", "token": "my-token" }
```

`type` matching is case-insensitive (`Basic`/`basic`, `Bearer`/`bearer`).

## Response Schema

Each value inside `response` can be:

- a static string
- a static number
- a static boolean
- a static null
- an object
- an array
- an item expression string: `$item.<itemName>.<expression>`
- a thing expression string: `$thing.<thingUID>.<expression>`

## Item Expressions

Top-level fields:
`state`, `lastStateUpdate`, `lastStateChange`, `name`, `type`, `acceptedDataTypes`, `acceptedCommandTypes`, `groups`, `tags`, `label`, `category`, `stateDescription`, `commandDescription`.

Examples:

- `$item.LivingRoomTemp.state`
- `$item.LivingRoomTemp.name,tags`
- `$item.LivingRoomTemp.stateDescription.minimum,maximum`
- `$item.LivingRoomTemp.commandDescription`

Date formatting:

- `$item.LivingRoomTemp.lastStateUpdate.[yyyy-MM-dd HH:mm:ss]`
- `$item.LivingRoomTemp.lastStateChange.[HH:mm:ss]`

Use comma-separated fields to return only selected subfields as an object.
For example, `$item.MyItem.name,tags` returns `{ "name": "...", "tags": [...] }`.

## Thing Expressions

Top-level fields:
`label`, `channels`, `channel`, `status`, `statusInfo`, `configuration`, `uid`, `thingTypeUid`, `properties`, `location`, `enabled`, `semanticEquipmentTag`.

Examples:

- `$thing.mqtt:topic:broker:device.status`
- `$thing.mqtt:topic:broker:device.label,enabled`
- `$thing.mqtt:topic:broker:device.statusInfo.status,statusDetail`
- `$thing.mqtt:topic:broker:device.channel.temperature.label`
- `$thing.mqtt:topic:broker:device.channels.label`
- `$thing.mqtt:topic:broker:device.channels.uid,label`

`channel.<channelId>` addresses a specific channel.
`channel.<channelId>` supports channel sub-commands like `label`, `uid`, `kind`, `configuration`, `properties`, `defaultTags`, and `autoUpdatePolicy`.
`channels` returns an array of all channels.
`channels.<sub-command>` applies the same channel sub-commands to all channels and returns an array of projected values or objects.

## HTTP Behavior

Endpoints are available under:

`http://<openhab-host>:8080/restify<path>`

If authorization is configured, the request must provide the matching `Authorization` header.

Error responses are JSON:

```json
{ "code": 401, "error": "Authorization required" }
```

User-facing error messages are translated via openHAB i18n.

## Full Example

### Thing File (`*.things`)

```things
Thing restify:endpoint:status "REST Status Endpoint" [
  path="/status",
  method="GET",
  endpoint="{\"authorization\":{\"type\":\"Bearer\",\"token\":\"abc123\"},\"response\":{\"server\":\"openHAB\",\"insideTemp\":\"$item.InsideTemperature.state\",\"insideMeta\":\"$item.InsideTemperature.name,tags\",\"boiler\":\"$thing.mqtt:topic:broker:boiler.status,statusInfo\"}}"
]
```

### Test Calls

```bash
curl -H "Authorization: Bearer abc123" http://localhost:8080/restify/status
```

```bash
curl -u api:secret http://localhost:8080/restify/private
```
