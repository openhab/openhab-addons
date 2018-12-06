/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public static final int LINK_BUTTON_NOT_PRESSED = 101;
    public static final int INTERNAL_ERROR = 901;

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
