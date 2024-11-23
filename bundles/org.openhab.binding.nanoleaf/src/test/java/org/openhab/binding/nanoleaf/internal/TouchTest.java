/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nanoleaf.internal.model.TouchEvent;
import org.openhab.binding.nanoleaf.internal.model.TouchEvents;

import com.google.gson.Gson;

/**
 * Test for the TouchEvents
 *
 * @author Stefan Höhn - Initial contribution
 */

@NonNullByDefault
public class TouchTest {

    private final Gson gson = new Gson();

    @Test
    public void testTheRightLayoutView() {
        String json = "{\"events\":[{\"panelId\":48111,\"gesture\":1}]}";

        TouchEvents touchEvents = gson.fromJson(json, TouchEvents.class);
        if (touchEvents == null) {
            touchEvents = new TouchEvents();
        }
        List<TouchEvent> events = touchEvents.getEvents();
        assertThat(events.size(), greaterThan(0));
        assertThat(events.size(), is(1));
        @Nullable
        TouchEvent touchEvent = events.get(0);
        assertThat(touchEvent.getPanelId(), is("48111"));
        assertThat(touchEvent.getGesture(), is(1));
    }
}
