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
package org.openhab.binding.loxone.internal.types;

/**
 * A state update event. It is used to defer and queue processing of Loxone state updates, so they are not processed in
 * the websocket thread.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxStateUpdate {
    private final LxUuid uuid;
    private final Object value;

    public LxStateUpdate(LxUuid uuid, Object value) {
        this.uuid = uuid;
        this.value = value;
    }

    public LxUuid getUuid() {
        return uuid;
    }

    public Object getValue() {
        return value;
    }
}
