/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.api;

/**
 * Hue API error response object
 * 
 * @author Dan Cunningham
 */
public class HueErrorResponse {
    public static final int UNAUTHORIZED = 1;
    public static final int NOT_AVAILABLE = 3;
    public static final int METHOD_NOT_AVAILABLE = 4;
    public static final int LINK_BUTTON_NOT_PRESSED = 101;
    public static final int INTERNAL_ERROR = 901;

    public HueErrorMessage error;

    public HueErrorResponse(int type, String address, String description) {
        super();
        this.error = new HueErrorMessage(type, address, description);
    }

    public class HueErrorMessage {
        public int type;
        public String address;
        public String description;

        public HueErrorMessage(int type, String address, String description) {
            super();
            this.type = type;
            this.address = address;
            this.description = description;
        }

    }
}
