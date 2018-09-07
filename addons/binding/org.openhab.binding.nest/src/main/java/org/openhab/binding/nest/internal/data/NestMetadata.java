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
 * The meta data in the data downloads from Nest.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class NestMetadata {

    private String accessToken;
    private String clientVersion;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NestMetadata other = (NestMetadata) obj;
        if (accessToken == null) {
            if (other.accessToken != null) {
                return false;
            }
        } else if (!accessToken.equals(other.accessToken)) {
            return false;
        }
        if (clientVersion == null) {
            if (other.clientVersion != null) {
                return false;
            }
        } else if (!clientVersion.equals(other.clientVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
        result = prime * result + ((clientVersion == null) ? 0 : clientVersion.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NestMetadata [accessToken=").append(accessToken).append(", clientVersion=")
                .append(clientVersion).append("]");
        return builder.toString();
    }

}
