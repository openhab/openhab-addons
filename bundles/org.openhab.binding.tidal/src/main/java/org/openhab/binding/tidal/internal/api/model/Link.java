package org.openhab.binding.tidal.internal.api.model;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Link {

    private String href;
    private String self;
}
