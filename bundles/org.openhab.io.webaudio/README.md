# Web Audio

This IO bundle provides an `AudioSink` that is capable of accepting "FixedLengthAudioStreams" and "URLAudioStreams".
It defines and registers a new `Event` called `PlayURLEvent` to let eventbus consumers know that an audio stream should be played.
