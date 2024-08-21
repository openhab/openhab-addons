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
package org.openhab.binding.helios.internal.ws.rest;

import com.google.gson.JsonObject;

/**
 * Helper class for encapsulating REST objects
 *
 * @author Karel Goderis - Initial contribution
 */
public class RESTEvent {
    public long id;
    public long utcTime;
    public long upTime;
    public String event;
    public JsonObject params;

    RESTEvent() {
    }
}
