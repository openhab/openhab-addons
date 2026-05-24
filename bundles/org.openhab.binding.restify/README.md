# RESTify Binding

The RESTify binding exposes configurable HTTP endpoints from openHAB.
Each endpoint runs a standard openHAB transformation and returns the transformation output as the HTTP response body.

## Supported Things

This binding currently provides one thing type.

- `restify:endpoint`
  Creates one HTTP endpoint under `/restify/<path>`.

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

Thing type: `restify:endpoint`.

| Name | Type | Required | Advanced | Description |
|------|------|----------|----------|-------------|
| `path` | text | yes | no | URL path for the endpoint. Must start with `/`. `/` alone is not allowed. |
| `method` | enum (`GET`,`POST`,`PUT`,`DELETE`) | yes | yes | HTTP method accepted by this endpoint. |
| `transformationPattern` | text list | yes | no | Standard openHAB transformation pattern that generates the response body. |
| `contentType` | text | no | yes | MIME content type returned by this endpoint. Defaults to `application/json`. |
| `authorizationType` | enum (`NONE`,`BASIC`,`BEARER`) | no | yes | Endpoint-specific authorization type. Defaults to `NONE`. |
| `username` | text | no | yes | Basic authorization username. Required when `authorizationType` is `BASIC`. |
| `password` | text | no | yes | Basic authorization password. Required when `authorizationType` is `BASIC`. |
| `token` | text | no | yes | Bearer authorization token. Required when `authorizationType` is `BEARER`. |

`transformationPattern` uses the same syntax as other openHAB channel transformations, for example `JS:status.js` or `JSONPATH:$.value`.
Multiple transformations can be chained by listing each transformation on a separate line, or by separating them with `∩`.

## Transformation Input

The transformation receives a JSON string with request metadata:

```json
{
  "method": "GET",
  "path": "/status",
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
  transformationPattern="JS:status.js",
  contentType="application/json",
  authorizationType="BEARER",
  token="abc123"
]
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
curl -H "Authorization: Bearer abc123" http://localhost:8080/restify/status
```
