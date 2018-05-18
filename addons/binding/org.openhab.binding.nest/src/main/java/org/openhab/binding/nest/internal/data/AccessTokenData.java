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
 * Deals with the access token data that comes back from Nest when it is requested.
 *
 * @author David Bennett - Initial Contribution
 */
public class AccessTokenData {

    private String accessToken;
    private Long expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AccessTokenData [accessToken=").append(accessToken).append(", expiresIn=").append(expiresIn)
                .append("]");
        return builder.toString();
    }

}
