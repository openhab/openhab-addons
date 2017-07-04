package org.openhab.binding.supla.internal.mappers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.openhab.binding.supla.internal.supla.entities.SuplaToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class GsonMapper implements JsonMapper {
    private final Gson gson;

    private static Gson buildGson() {
        return new GsonBuilder().registerTypeAdapter(SuplaToken.class, new TokenAdapter()).create();
    }

    GsonMapper(Gson gson) {
        this.gson = gson;
    }

    public GsonMapper() {
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

    private static final class TokenAdapter extends TypeAdapter<SuplaToken> {
        private final Logger logger = LoggerFactory.getLogger(TokenAdapter.class);

        @Override
        public void write(JsonWriter jsonWriter, SuplaToken suplaToken) throws IOException {
            jsonWriter.name("suplaToken").value(suplaToken.getToken()).name("expires_in").value(suplaToken.getValidTimeInSeconds())
                    .name("create_timestamp").value(Timestamp.valueOf(suplaToken.getCreateDate()).getTime());
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
        public SuplaToken read(JsonReader jsonReader) throws IOException {
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
                        logger.trace("Can't parse this name '{}'. Probably this does no harm", name);
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();

            return new SuplaToken(token, validTimeInSeconds, LocalDateTime.now());
        }
    }
}
