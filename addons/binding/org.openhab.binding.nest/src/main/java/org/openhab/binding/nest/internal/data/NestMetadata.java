/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * The meta data in the data downloades from nest.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestMetadata {
    @SerializedName("access_token")
    private String access_token;
    @SerializedName("client_version")
    private String client_version;

    public String getAccess_token() {
        return access_token;
    }

    public String getClient_version() {
        return client_version;
    }
}
