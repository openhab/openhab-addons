/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import java.io.IOException;

/**
 * Error or warning reurned by PLCComS server.
 *
 * @author Radovan Sninsky
 * @since 2018-03-10 00:18
 */
public class PlcComSEception extends IOException {

    private String type;
    private String code;

    /**
     * Returns type of error.
     *
     * @return ERROR or WARNING type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns error identification code.
     *
     * @return error code
     */
    public String getCode() {
        return code;
    }

    PlcComSEception(String type, String code, String message) {
        super(message);
        this.type = type;
        this.code = code;
    }

    @Override
    public String toString() {
        return String.format("%s: %s:%s %s", getClass().getCanonicalName(), type, code, getMessage());
    }
}
