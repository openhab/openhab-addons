# Action Template Interpreter

This interpreter does nothing on his own.
It provides you with a template system to match text commands to items and read its states or send commands to them.

When reading from an item you can transform its state to a human-readable text and template it into your defined response.

When writing to an item you can extract parts of the text and transform those in a valid item command.

This human language interpreter aims to have no language dependency as it does nothing out of the box, please report any incompatibility with your language.

Be encourage to read the examples at the end to get a general idea of what can be done.

## Web UI

The addon includes a small UI that allows you configure and test your action templates.

## Action Template Target:

This interpreter allows two ways to target items:

* You can link an action to a specific item by adding the custom metadata namespace 'actiontemplatehli' to it using the main UI.
* You can link an action to a subset of items by using included UI. Those actions should contain the $itemLabel placeholder which will be matched against all the compatible items labels.

Two important notes:

* Actions linked to items have prevalence over actions linked to a whole item type.
* On actions linked to an item type the itemLabel placeholder will always be applied (explained bellow). If there are multiple item labels detected on the input the actions linked to both will be scored, each using the correct itemLabel value, so there should be no collisions in case a template contains a token that matches an item label.

## Action Template Scoring

The scoring mechanism scores each action configuration to look for the best scored.
If a token fails the comparison, the action scores 0.
If all actions scored 0, none is executed.

The plugin scores by token equality; If one token fails the comparison the template will be scored 0.

The action template string is a list of tokens separated by white space.
You can use the ';' separator to provide alternative templates, the '|' to provide alternative tokens and the '?' suffix to make a token optional.
Here is an example of string: "what app|application is open? on $itemLabel;what is the app|application on $itemLabel".

Take in account that, as this is a token basis comparison, matching depends on the tokenizer you are using as they can produce different tokens for the same text.

## Action Template Options:

The location where action configurations are placed changes whether you are targeting an item or many, so take a look to the 'Action Template Target' to understand where to put those configurations.
Also, you can check the paths indicated on the examples at the end.

Actions can read the state from an item or send a command to it.
This is defined by the boolean field read which is 'false' by default.

When read is false:

* template: action template. (Required)
* value: value to be sent. It can be used to capture the transformed placeholder values. (Required)
* affectedTypes: item types affected by this action (at least one is required to work).
* affectedSemantics: item semantics affected by this action.
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* placeholders: defined placeholders that can be used on the template and replaced on the value.
* silent: boolean used to avoid confirmation message.
* groupTargets: when targeting a Group item, can be used to send the command to its member items instead. (Requires at least one affected type to take effect)

When read is true:

* template: action template. (Required)
* value: a template for the interpreter response, can use the placeholders symbols $groupLabel, $itemLabel and $state. (Required)
* emptyValue: An alternative response template. Is used when the state value is empty or NULL after the transforming it. The $groupLabel and $itemLabel placeholders are available.
* affectedTypes: item types affected by this action (at least one is required to work).
* affectedSemantics: item semantics affected by this action.
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* placeholders: only the placeholder with label state will be used, will apply its configured mappedValues on the state backwards.
* groupTargets: when targeting a Group item, can be used to send the command to its member items instead.  (Requires at least one affected type to take effect)

## Placeholders

This configuration allow you to define symbols to use on your templates.
You can define the sets of tokens to be matched and its required transformations.

A placeholder can be defined inside an action or as a shared one.

Those are its fields:

 * label: label for the placeholder, is prefixed with '$' and spaces are replaced by '_' to create the symbol you can use on the template (Required).
 * values: list of strings containing parts of the text to look for and capture this text as placeholder value.
 * mappedValues: object with keys and values of type strings, keys are used to match parts of the text and capture its value as placeholder value.

So the total list of terms to look for as valid placeholder value are the combination of the values, mappedValues keys.

Note that mappedValues are applied backwards to read actions so the mappedValues object should always contain the human readable text as keys and the state that represented it as value. This way you can reuse a placeholder between read and write actions. 

As a summary, using the placeholders you can configure how parts of the speech are converted into valid item values and backward.
The examples at the end of the document can help you to see it clearer.

There are some reserved placeholders, explained bellow.

### The 'itemLabel' Placeholder

The itemLabel placeholder is always applied when scoring actions linked to item types. It'll replace your item labels and synonyms (collisions will be reported in debug logs). Its value is only available for read actions.

### The 'groupLabel' Placeholder

The groupLabel placeholder is only available for read actions when targeting a group member.
When it's present, the 'itemLabel' placeholder will take the value of the target member label and the 'groupLabel' the label of the group.

### The 'state' Placeholder

It's used to access the value on the read actions, you can configure mappedValues for it (remember those are applied backwards).

### The 'itemOption' Placeholder

This placeholder is available for both write and read actions and doesn't need to be configured.

When read is false, the 'itemOption' placeholder will be computed from the item command description options, or from the state description options if the command description options are not present.

When read is true, the 'itemOption' placeholder will be computed from the item state description options.

Note that, when targeting multiple group members, text search is done by merging all available member value options, but the value transformation is done using just the target member item options.

### The '*' Placeholder (the dynamic placeholder)

The dynamic placeholder is designed to capture free text. The captured value is exposed to the value under the symbol '$*' as other placeholders.
It has some restrictions:

* Can not be the only token on the template.
* Can not be used as an optional token.
* Can not be used multiple times on the same template alternative.

Note that the dynamic placeholder does not score. This way you can use it to fallback other sentences.

This example can help you to understand how this works:

You have an action with the template "play $* on living room" and another action with the template "play $musicAuthor on living room" and assuming 'mozart' is a valid value for the placeholder '$musicAuthor'.

The sentence "play mozart on living room" will score 80% when compared with the template containing the dynamic placeholder and 100% when compared with the one without it.
The action with template "play $musicAuthor on living room" will be executed.

The sentence "play beethoven on living room" will score 80% when compared with the template containing the dynamic placeholder and 0 when compared with the one without it, as 'beethoven' is not a valid value for the '$musicAuthor' placeholder.
The action with template "play $* on living room" will be executed.

### Group Targets:

When the target of an action is a group item, you can target its members instead.

You can use the following fields:

* affectedTypes: item types affected by this action (at least one is required or all the group targets configurations is ignored).
* affectedSemantics: item semantics affected by this action.
* requiredTags: allow to restrict the items targeted by its tags by ignoring items not having all these tags.
* recursive: when matching by itemType, look for group members in a recursive way, default true.
* mergeState: on a read action when matching by itemType, merge the item states by performing an AND operation, only allowed for 'Switch' and 'Contact' item types and for a single affectedType, default false.

## Text Preprocessing

The interpreter needs to match the input text with a target item and action configuration, to know what to do.
To do so, it needs the tokens and optionally the POS tags and the lemmas.

### Tokenizer

Tokenize: First step of the recognition is to split the text, each of these parts is called token. 

You can provide a custom model at '<OPENHAB_USERDATA>/actiontemplatehli/token.bin', otherwise it will use the built-in simple tokenizer or whitespace tokenizer (configurable).

Here you have an example of the built-in ones:

* Using the white space tokenizer "What time is it?" produces the tokens "what" "time" "is" "it?"
* Using the simple tokenizer "What time is it?" produces the tokens "what" "time" "is" "it" "?"

Tokenizing the text is enough to use the action type 'tokens' as tokens are the only ones required for scoring (but the option 'optionalLanguageTags' will not take effect unless you have the POS language tags).

### POSTagger (language tags)

Part Of Speech (POS) tagging: categorizing tokens in a text, depending on the definition of the token and its context.

You need to provide a model for POS tagging at '<OPENHAB_USERDATA>/actiontemplatehli/pos.bin' for your language. 
Please note that these labels may be different depending on the model, please refer to your model's documentation.
As an example:

The tokens "that,sounds,good" produces the tags "DT,VBZ,JJ".

You can match against these tags by prefixing a token by <tag> in a template.
As an example of this you can use the template "<tag>DT sound good" to match the tokens "that,sounds,good". 

You need the correct language tags for the lemmatizer to work.

### Lemmatizer

Lemmatize: is the process of getting a generic representation of the tokens, each of it is called lemma. (Example of one token to lemma conversion: 'is' -> 'be').

You need to provide a model for the lemmatizer at '<OPENHAB_USERDATA>/actiontemplatehli/lemma.bin' for your language.
This will produce a lemma for each token.

You can match against these lemmas by prefixing a token by <lemma> in a template.
As an example of this you can use the template "he <lemma>be good" to match the tokens "he,is,good".

Note that you need the POS language tags for your language, the ones covered on the previous section, for the lemmatizer to work.

## NLP Models

You can find models provided by OpenNLP for some languages [here](https://opennlp.apache.org/models.html) and [here](http://opennlp.sourceforge.net/models-1.5/).
Those are just required to use the matching by lemma or tag functionality.

## Interpreter Configuration

| Config                  |  Group   |  Type   |   Default          | Description                                                                                                   |
|-------------------------|----------|---------|--------------------|---------------------------------------------------------------------------------------------------------------|
| useSimpleTokenizer      | nlp      | boolean | false              | Prefer simple tokenizer over white space tokenizer.                                                           |
| detokenizeOptimization  | nlp      | boolean | true               | Enables build-in detokenization based on original text, otherwise string join by space is used.               |
| commandSentMessage      | messages | text    | Done               | Message for successful command.                                                                               |
| unhandledMessage        | messages | text    | I can not do that  | Message for unsuccessful action.                                                                              |
| failureMessage          | messages | text    | There was an error | Message for error during processing.                                                                          |

## Single Item Actions:

To link an action to an specific item you should do it using the main interface and editing its metadata. These are some examples:

### Switch item action configs example:

This example contains the item metadata to add an action to an item with type 'Switch''.
Add a custom metadata 'actiontemplatehli' to the 'Switch' item with the following:

```yaml
value: ""
config:
  placeholders:
    - label: onOff
      mappedValues:
        "turn on": ON
        "turn off": OFF
  template: $onOff $itemLabel
  value: $onOff
```

### Dynamic placeholder, sending a message example:

This example contains the item metadata to add an action that uses the dynamic placeholder.
Add a custom metadata 'actiontemplatehli' to a String item with the following:

```yaml
value: ""
config:
  placeholders:
    - label: contact
      values:
        - Andrea
        - Jacob
        - Raquel
  silent: true
  template: send message $* to $contact
  value: $contact:$*
```
