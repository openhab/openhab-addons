/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
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
        @Nullable
        TouchEvents touchEvents = gson.fromJson(json, TouchEvents.class);
        assertThat(touchEvents.getEvents().size(), greaterThan(0));
        assertThat(touchEvents.getEvents().size(), is(1));
        @Nullable
        TouchEvent touchEvent = touchEvents.getEvents().get(0);
        assertThat(touchEvent.getPanelId(), is("48111"));
        assertThat(touchEvent.getGesture(), is(1));
    }
}
