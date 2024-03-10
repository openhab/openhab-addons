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
package org.openhab.io.hueemulation.internal.dto.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Hue API response base type
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueResponse {
    public static final int UNAUTHORIZED = 1;
    public static final int INVALID_JSON = 2;
    public static final int NOT_AVAILABLE = 3;
    public static final int METHOD_NOT_ALLOWED = 4;
    public static final int ARGUMENTS_INVALID = 7;
    public static final int SENSOR_NOT_CLIP_SENSOR = 8;
    public static final int LINK_BUTTON_NOT_PRESSED = 101;
    public static final int INTERNAL_ERROR = 901;

    public static final int RULE_ENGINE_FULL = 601; // The Rule Engine has reached its maximum capacity of 100 rules.
    public static final int CONDITION_ERROR = 607; // Rule conditions contain errors or operator combination is not
                                                   // allowed
    public static final int ACTION_ERROR = 608; // Rule actions contain errors or multiple actions with the same
                                                // resource address.
    public static final int TOO_MANY_ITEMS = 11; // Too many items in the list (too many conditions or too many actions)

    public final @Nullable HueErrorMessage error;
    public final @Nullable HueSuccessResponse success;

    public HueResponse(HueErrorMessage error) {
        this.error = error;
        this.success = null;
    }

    public HueResponse(HueSuccessResponse success) {
        this.error = null;
        this.success = success;
    }

    public static class HueErrorMessage {
        public final String address;
        public final String description;
        public final int type;

        public HueErrorMessage(int type, String address, String description) {
            this.type = type;
            this.address = address;
            this.description = description;
        }
    }
}
