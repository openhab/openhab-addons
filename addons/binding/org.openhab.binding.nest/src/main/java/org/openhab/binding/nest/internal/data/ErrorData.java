/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

/**
 * The data of Nest API errors.
 *
 * @author Wouter Born - Improve exception handling
 */
public class ErrorData {

    private String error;
    private String type;
    private String message;
    private String instance;

    public String getError() {
        return error;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorData [error=").append(error).append(", type=").append(type).append(", message=")
                .append(message).append(", instance=").append(instance).append("]");
        return builder.toString();
    }

}
