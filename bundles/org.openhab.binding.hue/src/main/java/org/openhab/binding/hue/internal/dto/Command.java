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
package org.openhab.binding.hue.internal.dto;

import com.google.gson.Gson;

/**
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 * @author Samuel Leisering - changed Command visibility to public
 */
public class Command {
    public String key;
    public Object value;

    public Command(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    String toJson() {
        return "\"" + key + "\":" + new Gson().toJson(value);
    }
}
