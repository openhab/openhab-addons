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
package org.openhab.binding.hue.internal.api.dto.clip1;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

/**
 * Basic hue object information.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 * @author Samuel Leisering - introduced Sensor support, renamed supertype to HueObject
 */
public class HueObject {
    public static final Type GSON_TYPE = new TypeToken<Map<String, HueObject>>() {
    }.getType();

    private String id;
    private String name;

    HueObject() {
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the id of the light.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the light.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
}
