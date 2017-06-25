package org.openhab.binding.supla.internal.server.mappers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import org.openhab.binding.supla.internal.server.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class JsonMapper implements Mapper {
    private final Gson gson;

    private static Gson buildGson() {
        return new GsonBuilder().registerTypeAdapter(Token.class, new TokenAdapter()).create();
    }

    JsonMapper(Gson gson) {
        this.gson = gson;
    }

    public JsonMapper() {
        this(buildGson());
    }

    @Override
    public String map(Object o) {
        return gson.toJson(o);
    }

    @Override
    public <T> T to(Class<T> clazz, String string) {
        return gson.fromJson(string, clazz);
    }

    @Override
    public <T> T to(Type type, String string) {
        return gson.fromJson(string, type);
    }

    private static final class TokenAdapter extends TypeAdapter<Token> {
        private final Logger logger = LoggerFactory.getLogger(TokenAdapter.class);

        @Override
        public void write(JsonWriter jsonWriter, Token token) throws IOException {
            jsonWriter.name("token").value(token.getToken()).name("expires_in").value(token.getValidTimeInSeconds())
                    .name("create_timestamp").value(Timestamp.valueOf(token.getCreateDate()).getTime());
            jsonWriter.close();
        }

        // {
        // "access_token": "MjQ3OWNkYWNkNGVlMzkwMzEzZmY5OTI1OGZhZDI2MGJhY2Y2NjBhNzVjMjYyOTU1NjNmZTllNzA3YTZkN2NhMw",
        // "expires_in": 30,
        // "token_type": "bearer",
        // "scope": "restapi",
        // "refresh_token": "ZDYyOTM5MTk3NmFlMDU1MDdmMjdiOTAwOTM4MWIwZjlkMzVmNWIzN2I0YTYzMjU3ZWM5NzFkZTQxYzQ0YzQ2NA"
        // }
        @Override
        public Token read(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            String token = null;
            int validTimeInSeconds = -1;
            while (jsonReader.hasNext()) {
                final String name = jsonReader.nextName();
                switch (name) {
                    case "access_token":
                        token = jsonReader.nextString();
                        break;
                    case "expires_in":
                        validTimeInSeconds = jsonReader.nextInt();
                        break;
                    default:
                        logger.warn("Can't parse this name '" + name + "'!");
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();

            return new Token(token, validTimeInSeconds, LocalDateTime.now());
        }
    }
}
