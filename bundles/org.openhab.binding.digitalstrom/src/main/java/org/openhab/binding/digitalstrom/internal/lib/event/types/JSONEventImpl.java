/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link JSONEventImpl} is the implementation of the {@link Event}.
 *
 * @author Alexander Betker
 */
public class JSONEventImpl implements Event {

    private List<EventItem> eventItemList;

    /**
     * Creates a new {@link JSONEventImpl} from the given digitalSTROM-Event {@link JsonArray}.
     *
     * @param jsonEventArray must not be null
     */
    public JSONEventImpl(JsonArray jsonEventArray) {
        this.eventItemList = new LinkedList<>();
        for (int i = 0; i < jsonEventArray.size(); i++) {
            if (jsonEventArray.get(i) instanceof JsonObject) {
                this.eventItemList.add(new EventItemImpl((JsonObject) jsonEventArray.get(i)));
            }
        }
    }

    @Override
    public List<EventItem> getEventItems() {
        return eventItemList;
    }
}
