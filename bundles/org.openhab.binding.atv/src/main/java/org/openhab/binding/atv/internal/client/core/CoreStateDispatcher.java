/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Per-device dispatcher for internal state updates, shared by all protocols of one device.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CoreStateDispatcher extends MessageDispatcher<UpdatedState, StateMessage> {

    /**
     * Creates a dispatcher delivering state messages on the given device loop.
     *
     * @param loop device loop used for listener invocation
     */
    public CoreStateDispatcher(DeviceLoop loop) {
        super(loop);
    }
}
