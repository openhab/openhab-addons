/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import org.openhab.binding.icloud.internal.json.JSONRootObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Extracts iCloud device information from a given JSON string
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class DeviceInformationParser {
    private final Gson gson = new GsonBuilder().create();

    public JSONRootObject parse(String json) {
        return gson.fromJson(json, JSONRootObject.class);
    }
}
