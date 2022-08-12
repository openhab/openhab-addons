# Ruby Transformation Service

Transform an input to an output using Ruby. 

It expects the transformation rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations, one should use subfolders.

Simple transformation rules can also be given as a inline script.
Inline script should be start by `|` character following the Ruby.
Beware that complex inline script could cause issues to e.g. item file parsing.

## Examples

Let's assume we have received a string containing `foo bar baz` and we're looking for a length of the last word (`baz`).

transform/get_value.rb:

```
@input.split(" ").last.length
```

Ruby transformation syntax also supports additional parameters which can be passed to the script. 
This can prevent redundancy when transformation is needed for several use cases, but with small adaptations.
Additional parameters can be passed to the script via [URI](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) query syntax.

As `input` name is reserved for transformed data, it can't be used in query parameters. 
Also `?` and `&` characters are reserved, but if they need to passed as additional data, they can be escaped according to URI syntax.


transform/scale.rb:
```
@input.to_f * correctionFactor.to_f / divider.to_f
```

`transform/scale.rb?correctionFactor=1.1&divider=10`

Following example will return value `23.54` when `input` data is `214`.

### Inline script example:

Normally Ruby transformation is given by filename, e.g. `RUBY(transform/get_value.rb)`.
Inline script can be given by `|` character following the Ruby, e.g. `RUBY(| @input / 10)`.
