/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.exceptions;

import com.google.gson.JsonObject;

/**
 * Exception to be thrown for HTTP requests that doesn't contains JSON as response content or the JSON content is
 * invalid
 *
 * @author Lyubomir Papazov - Initial contribution
 */
public class UnsuccessfulJsonResponseException extends Exception {

    private JsonObject jsonResponse;

    public UnsuccessfulJsonResponseException(JsonObject jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public JsonObject getResponse() {
        return jsonResponse;
    }

}
