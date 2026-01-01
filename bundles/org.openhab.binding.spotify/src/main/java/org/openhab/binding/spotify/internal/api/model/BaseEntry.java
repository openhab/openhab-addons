package org.openhab.binding.spotify.internal.api.model;

import org.openhab.core.media.BaseDto;

public class BaseEntry extends BaseDto {
    @Override
    public String getKey() {
        return getUri();
    }

}
