# Jinja Transformation Service

Transforms a value using a Jinja template.

The main purpose of this transformer is use in Home Assistant discovery.
Therefore, not all features of the Home Assistant templating engine are supported.
See also: [Processing incoming data](https://www.home-assistant.io/docs/configuration/templating/#processing-incoming-data).

## Available variables

| Variable   | Description                        |
|------------|------------------------------------|
| value      | The incoming value.                |
| value_json | The incoming value parsed as JSON. |

## Examples

### Basic Examples

#### Incoming data

Given the value

```json
{"Time":"2019-01-05T22:45:12","AM2301":{"Temperature":4.7,"Humidity":93.7},"TempUnit":"C"}
```

the template

```text
{{ value_json['AM2301'].Temperature }}
```

extracts the string `4.7`.

#### Outgoing data

The JINJA transformation can be used to publish simple JSON strings through, for example, the HTTP binding’s `commandTransformation` parameter.

Say we have a String Item which holds the following value:

```text
This is my string
```

Adding the following into the `commandTransformation` parameter of your HTTP Thing channel

```text
JINJA:{"msgtype":"m.text", "body":"{{value}}"}
```

will send the following string out of openHAB:

```json
{"msgtype":"m.text", "body":"This is my string"}
```

`{{value}}` will be replaced by whatever the value of your Item is.

Note that if using .things files you must escape quotation marks, for example:

```text
commandTransformation = "JINJA:{\"msgtype\":\"m.text\", \"body\":\"{{value}}\"}"
```

## Further Reading

- Wikipedia on [Jinja](https://en.wikipedia.org/wiki/Jinja_%28template_engine%29).
- Home Assistant [discovery](https://www.home-assistant.io/docs/mqtt/discovery/).
- Home Assistant [templating](https://www.home-assistant.io/docs/configuration/templating/).
