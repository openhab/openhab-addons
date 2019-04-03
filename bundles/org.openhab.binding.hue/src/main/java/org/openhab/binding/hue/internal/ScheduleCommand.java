/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal;

import com.google.gson.JsonElement;

/**
 * Information about a scheduled command.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class ScheduleCommand {
    private String address;
    private String method;
    private JsonElement body;

    ScheduleCommand(String address, String method, JsonElement body) {
        this.address = address;
        this.method = method;
        this.body = body;
    }

    /**
     * Returns the relative request url.
     *
     * @return request url
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the request method.
     * Can be GET, PUT, POST or DELETE.
     *
     * @return request method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the request body.
     *
     * @return request body
     */
    public String getBody() {
        return body.toString();
    }
}
