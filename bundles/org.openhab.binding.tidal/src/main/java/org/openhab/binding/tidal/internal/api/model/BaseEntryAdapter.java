package org.openhab.binding.tidal.internal.api.model;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class BaseEntryAdapter extends TypeAdapter<BaseEntry> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, BaseEntry value) throws IOException {
        gson.toJson(value, value.getClass(), out);
    }

    @Override
    public BaseEntry read(JsonReader in) throws IOException {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        Class<? extends BaseEntry> concreteClass;
        switch (type) {
            case "albums":
                concreteClass = Album.class;
                break;
            case "artworks":
                concreteClass = Artwork.class;
                break;
            case "artists":
                concreteClass = Artist.class;
                break;
            case "tracks":
                concreteClass = Track.class;
                break;
            case "playlists":
                concreteClass = Playlist.class;
                break;
            case "videos":
                concreteClass = Video.class;
                break;
            default:
                throw new JsonParseException("Type inconnu: " + type);
        }

        return gson.fromJson(jsonObject, concreteClass);
    }
}