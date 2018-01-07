/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import com.google.gson.annotations.SerializedName;

/**
 * A JSON response to a call to send a command to Miniserver's control at
 * http://miniserver/jdev/sps/io/{uuid}/{command}.
 * <p>
 * This structure is used for parsing with Gson library.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxJsonResponse {

    class LxJsonSubResponse {
        String control;
        String value;
        @SerializedName("Code")
        int code;
    }

    @SerializedName("LL")
    LxJsonSubResponse subResponse;
}
