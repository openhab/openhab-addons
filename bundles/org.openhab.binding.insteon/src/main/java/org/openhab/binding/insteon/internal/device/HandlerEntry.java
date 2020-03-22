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
package org.openhab.binding.insteon.internal.device;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Ugly little helper class to facilitate late instantiation of handlers
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class HandlerEntry {
    HashMap<String, @Nullable String> params;
    String name;

    HandlerEntry(String name, HashMap<String, @Nullable String> params) {
        this.name = name;
        this.params = params;
    }

    HashMap<String, @Nullable String> getParams() {
        return params;
    }

    String getName() {
        return name;
    }
}
