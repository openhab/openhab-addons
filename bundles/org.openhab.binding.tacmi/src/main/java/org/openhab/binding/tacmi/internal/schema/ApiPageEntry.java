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
package org.openhab.binding.tacmi.internal.schema;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

/**
 * The {@link ApiPageEntry} class contains mapping information for an entry of
 * the API page.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class ApiPageEntry {

    static enum Type {
        READ_ONLY_SWITCH(true),
        READ_ONLY_NUMERIC(true),
        NUMERIC_FORM(false),
        SWITCH_BUTTON(false),
        SWITCH_FORM(false),
        READ_ONLY_STATE(true),
        STATE_FORM(false);

        public final boolean readOnly;

        private Type(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }

    /**
     * type of this entry
     */
    public final Type type;

    /**
     * The channel for this entry
     */
    public final Channel channel;

    /**
     * internal address for this channel
     */
    public final @Nullable String address;

    /**
     * data for handle 'changerx2' form fields
     */
    public final @Nullable ChangerX2Entry changerX2Entry;

    /**
     * The last known state for this item...
     */
    private State lastState;

    /**
     * Timestamp (epoch ms) when last 'outgoing' command was sent.
     * Required for de-bounce overlapping effects when status-poll's and updates overlap.
     */
    private long lastCommandTS;

    protected ApiPageEntry(final Type type, final Channel channel, @Nullable final String address,
            @Nullable ChangerX2Entry changerX2Entry, State lastState) {
        this.type = type;
        this.channel = channel;
        this.address = address;
        this.changerX2Entry = changerX2Entry;
        this.lastState = lastState;
    }

    public void setLastState(State lastState) {
        this.lastState = lastState;
    }

    public State getLastState() {
        return lastState;
    }

    public long getLastCommandTS() {
        return lastCommandTS;
    }

    public void setLastCommandTS(long lastCommandTS) {
        this.lastCommandTS = lastCommandTS;
    }
}
