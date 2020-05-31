/*
 * Copyright (C) 2016 Gson Type Adapter Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Imported from https://github.com/google-gson/typeadapters/tree/master/jsr310/src
 * and repackaged to avoid the default package.
 */
package org.openhab.binding.sleepiq.api.impl.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.function.Function;

/**
 * Abstract type adapter for jsr310 date-time types.
 *
 * @author Christophe Bornet
 */
abstract class TemporalTypeAdapter<T> extends TypeAdapter<T> {

  Function<String, T> parseFunction;

  TemporalTypeAdapter(Function<String, T> parseFunction) {
    Objects.requireNonNull(parseFunction);
    this.parseFunction = parseFunction;
  }

  @Override
  public void write(JsonWriter out, T value) throws IOException {
    if (value == null) {
      out.nullValue();
    } else {
      out.value(value.toString());
    }
  }

  @Override
  public T read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    String temporalString = preProcess(in.nextString());
    return parseFunction.apply(temporalString);
  }

  public String preProcess(String in) {
    return in;
  }
}
