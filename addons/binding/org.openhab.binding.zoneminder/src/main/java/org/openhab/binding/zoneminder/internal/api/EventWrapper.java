/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.api;

/**
 * The Class Event is a helper class that wraps JSON data from ZoneMinder API call.
 *
 * @author Martin S. Eskildsen
 */
public class EventWrapper {
    private Event Event;

    public Event getEvent() {
        return this.Event;
    }

    public void setEvent(Event Event) {
        this.Event = Event;
    }

    private String thumbData;

    public String getThumbData() {
        return this.thumbData;
    }

    public void setThumbData(String thumbData) {
        this.thumbData = thumbData;
    }
}
