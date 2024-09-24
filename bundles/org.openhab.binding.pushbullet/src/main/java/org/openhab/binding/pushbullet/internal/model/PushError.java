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
package org.openhab.binding.pushbullet.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents errors in the response fetched from the API.
 *
 * @author Hakan Tandogan - Initial contribution
 * @author Hakan Tandogan - Migrated from openHAB 1 action with the same name
 * @author Jeremy Setton - Add link and file push type support
 */
@NonNullByDefault
public class PushError {

    @SerializedName("type")
    private @Nullable String type;

    @SerializedName("message")
    private @Nullable String message;

    @SerializedName("param")
    private @Nullable String param;

    @SerializedName("cat")
    private @Nullable String cat;

    public @Nullable String getType() {
        return type;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public @Nullable String getParam() {
        return param;
    }

    public @Nullable String getCat() {
        return cat;
    }

    @Override
    public String toString() {
        return "PushError {type='" + type + "', message='" + message + "', param='" + param + "', cat='" + cat + "'}";
    }
}
