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
package org.openhab.binding.heos.internal.json;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * Wrapper used around HeosJsonObject used for the initial JSON parsing of the result
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
class HeosJsonWrapper {
    HeosJsonObject heos = new HeosJsonObject();
    @Nullable
    JsonElement payload;
    @Nullable
    List<Map<String, List<HeosOption>>> options;
}
