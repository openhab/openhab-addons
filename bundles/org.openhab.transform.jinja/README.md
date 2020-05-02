# Jinja Transformation Service

Transforms a value using a jinja template. 

The main purpose of this transformer is the use in the home assistant discovery. Therfore not all features of the home assistant templating are supported.
Basically on [Processing incoming data](https://www.home-assistant.io/docs/configuration/templating/#processing-incoming-data)

## Available variables

| Variable   | Description                        |
|------------|------------------------------------|
| value      | The incoming value.                |
| value_json | The incoming value parsed as JSON. |

## Examples

### Basic Example

Given the value

```
{"Time":"2019-01-05T22:45:12","AM2301":{"Temperature":4.7,"Humidity":93.7},"TempUnit":"C"}
```

the template

::: v-pre
`{{value_json['AM2301'].Temperature}}`
:::

extracts the string `4.7`.

## Further Reading

* Wikipedia on [Jinja](https://en.wikipedia.org/wiki/Jinja_(template_engine).
* Home assistant [discovery](https://www.home-assistant.io/docs/mqtt/discovery/).
* Home assistant [templating](https://www.home-assistant.io/docs/configuration/templating/).
