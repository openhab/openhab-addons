/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxXdslStatus} is the Java class used to map the "XdslStatus"
 * structure used by the response of the connection xDSL status API
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxXdslStatus {
    public static class InternalXdslStatus {
        private String status;
        private String protocol;
        private String modulation;
        private long uptime;
    }

    private InternalXdslStatus status;

    public String getStatus() {
        return status.status;
    }

    public String getProtocol() {
        return status.protocol;
    }

    public String getModulation() {
        return status.modulation;
    }

    public long getUptime() {
        return status.uptime;
    }
}
