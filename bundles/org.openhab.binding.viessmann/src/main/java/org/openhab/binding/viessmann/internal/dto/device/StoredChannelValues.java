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
package org.openhab.binding.viessmann.internal.dto.device;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link StoredChannelValues} provides the values from a device
 *
 * @author Ronny Grun - Initial contribution
 */
public class StoredChannelValues {
    public Map<String, String> prop = new HashMap<>();

    public void putProperty(String key, String value) {
        prop.put(key, value);
    }

    public String getProperty(String key) {
        return prop.get(key);
    }
}
