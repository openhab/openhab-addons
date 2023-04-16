/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.digitalstrom.internal.lib.event.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.digitalstrom.internal.lib.event.constants.EventResponseEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EventItemImpl} is the implementation of the {@link EventItem}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public class EventItemImpl implements EventItem {

    private final String name;
    private Map<EventResponseEnum, String> properties;
    private Map<EventResponseEnum, String> source;

    /**
     * Creates a new {@link EventItemImpl} from the given digitalSTROM-Event-Item {@link JsonObject}.
     *
     * @param jsonEventItem must not be null
     */
    public EventItemImpl(JsonObject jsonEventItem) {
        name = jsonEventItem.get(JSONApiResponseKeysEnum.NAME.getKey()).getAsString();

        if (jsonEventItem.get(JSONApiResponseKeysEnum.PROPERTIES.getKey()).isJsonObject()) {
            Set<Entry<String, JsonElement>> propObjEntrySet = jsonEventItem
                    .get(JSONApiResponseKeysEnum.PROPERTIES.getKey()).getAsJsonObject().entrySet();
            properties = new HashMap<>(propObjEntrySet.size());
            for (Entry<String, JsonElement> entry : propObjEntrySet) {
                if (EventResponseEnum.containsId(entry.getKey())) {
                    addProperty(EventResponseEnum.getProperty(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }
        if (jsonEventItem.get(JSONApiResponseKeysEnum.SOURCE.getKey()).isJsonObject()) {
            Set<Entry<String, JsonElement>> sourceObjEntrySet = jsonEventItem
                    .get(JSONApiResponseKeysEnum.SOURCE.getKey()).getAsJsonObject().entrySet();
            source = new HashMap<>(sourceObjEntrySet.size());
            for (Entry<String, JsonElement> entry : sourceObjEntrySet) {
                if (EventResponseEnum.containsId(entry.getKey())) {
                    addSource(EventResponseEnum.getProperty(entry.getKey()), entry.getValue().getAsString());
                }
            }
        }
    }

    private void addProperty(EventResponseEnum propertieKey, String value) {
        properties.put(propertieKey, value);
    }

    private void addSource(EventResponseEnum sourceKey, String value) {
        source.put(sourceKey, value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<EventResponseEnum, String> getProperties() {
        return properties;
    }

    @Override
    public Map<EventResponseEnum, String> getSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EventItemImpl [name=" + name + ", properties=" + properties + ", source=" + source + "]";
    }
}
