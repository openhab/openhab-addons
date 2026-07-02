# HTTP Hook

The HTTP Hook binding exposes configurable HTTP endpoints from openHAB.
Each endpoint runs a standard openHAB transformation and returns the transformation output as the HTTP response body.

## Supported Things

This binding currently provides one thing type.

- `httphook:endpoint`
  Creates HTTP endpoints under `/httphook/<thing-id>/<channel-id>`.

## Discovery

This binding does not provide auto-discovery.
Create endpoint things manually.

## Binding Configuration

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `enforceAuthentication` | boolean | `false` | If `true`, every endpoint must define authorization or use a matching binding default. |
| `defaultBasic` | text | empty | Default Basic credentials in `username:password` format, used when endpoint authorization is not configured and the request has a `Basic` authorization prefix. |
| `defaultBearer` | text | empty | Default Bearer token, used when endpoint authorization is not configured and the request has a `Bearer` authorization prefix. |

## Thing Configuration

Thing type: `httphook:endpoint`.

| Name | Type | Required | Advanced | Description |
|------|------|----------|----------|-------------|
| `authorizationType` | enum (`NONE`,`BASIC`,`BEARER`) | no | yes | Endpoint-specific authorization type. Defaults to `NONE`. |
| `username` | text | no | yes | Basic authorization username. Required when `authorizationType` is `BASIC`. |
| `password` | text | no | yes | Basic authorization password. Required when `authorizationType` is `BASIC`. |
| `token` | text | no | yes | Bearer authorization token. Required when `authorizationType` is `BEARER`. |

## Channel Configuration

Channel type: `httphook:response`.

Each response channel creates one HTTP endpoint. Its URL is derived from the Thing id and channel id, so a channel
`status` on Thing `httphook:endpoint:frame` is available at `/httphook/frame/status`.

| Name | Type | Required | Advanced | Description |
|------|------|----------|----------|-------------|
| `method` | enum (`GET`,`POST`,`PUT`,`DELETE`) | yes | no | HTTP method accepted by this response channel. |
| `transformationPattern` | text list | yes | no | Standard openHAB transformation pattern that generates the response body. |
| `contentType` | text | no | yes | MIME content type returned by this response channel. Defaults to `application/json`. |

`transformationPattern` uses the same syntax as other openHAB channel transformations, for example `JS:status.js` or `JSONPATH:$.value`.
Multiple transformations can be chained by listing each transformation on a separate line, or by separating them with `∩`.

## Transformation Input

The transformation receives a JSON string with request metadata:

```json
{
  "method": "GET",
  "path": "/frame/status",
  "queryString": "format=json",
  "headers": {
    "User-Agent": ["curl/8.0.0"]
  },
  "remoteAddr": "127.0.0.1",
  "body": ""
}
```

The transformation output is returned unchanged.
It can be JSON, text, CSV, XML, or any other format.
Set `contentType` to match the output.

## HTTP Behavior

Endpoints are available under:

`http://<openhab-host>:8080/httphook/<thing-id>/<channel-id>`

If authorization is configured, the request must provide the matching `Authorization` header.

Error responses are JSON:

```json
{ "code": 401, "error": "Authorization required" }
```

User-facing error messages are translated via openHAB i18n.

## Full Example

### Thing File (`*.things`)

```things
Thing httphook:endpoint:frame "HTTP Hook Frame Endpoint" [
  authorizationType="BEARER",
  token="abc123"
] {
  Channels:
    Type response : status "Status Response" [
      method="GET",
      transformationPattern="JS:status.js",
      contentType="application/json"
    ]
}
```

### JavaScript Transformation (`transform/status.js`)

```javascript
JSON.stringify({
  server: "openHAB",
  insideTemp: items.InsideTemperature.state,
  request: JSON.parse(input).path
});
```

### Test Call

```bash
curl -H "Authorization: Bearer abc123" http://localhost:8080/httphook/frame/status
```
