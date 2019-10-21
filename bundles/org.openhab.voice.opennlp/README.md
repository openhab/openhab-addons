# OpenNLP Interpreter

## Overview

This natural language processor (OpenNLP) bundle is based on [Apache OpenNLP](https://opennlp.apache.org) for intent classification and entity extraction (thanks to [nlp-intent-toolkit](https://github.com/mlehman/nlp-intent-toolkit)).

It is composed of a modular intent-based skill system with learning data provisioning (basic skills to retrieve item statuses, historical data and send basic commands are built-in, but more can be injected by other OSGi dependency injection).

This bundle works great in tandem with the openHAB voice services to provide a [Human Language Interpreter](http://docs.openhab.org/configuration/multimedia.html#human-language-interpreter). Coupled with speech-to-text and text-to-speech engines it allows to build privacy-focused specialized voice assistant services.

## Service Configuration

Using your favourite configuration UI (e.g. PaperUI) edit **Services/Voice/OpenNLP Interpreter** settings and set 

* **Tokenizer** - The tokenizer to be used, either Whitespace or Alphanumeric. The alphanumeric tokenizer removes punctuation and might help with languages where entities might not always be enclosed between whitespaces (French, Spanish, Italian... Example: "l'humidité", "l'alarme").
