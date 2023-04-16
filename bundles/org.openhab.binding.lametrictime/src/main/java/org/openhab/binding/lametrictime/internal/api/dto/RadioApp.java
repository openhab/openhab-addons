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
package org.openhab.binding.lametrictime.internal.api.dto;

import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;

/**
 * Implementation class for the RadioApp.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class RadioApp extends CoreApplication {
    private static final String NAME = "com.lametric.radio";

    private static final String ACTION_NEXT = "radio.next";
    private static final String ACTION_PLAY = "radio.play";
    private static final String ACTION_PREV = "radio.prev";
    private static final String ACTION_STOP = "radio.stop";

    public RadioApp() {
        super(NAME);
    }

    public CoreAction next() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_NEXT));
    }

    public CoreAction play() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PLAY));
    }

    public CoreAction previous() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_PREV));
    }

    public CoreAction stop() {
        return new CoreAction(this, new UpdateAction().withId(ACTION_STOP));
    }
}
