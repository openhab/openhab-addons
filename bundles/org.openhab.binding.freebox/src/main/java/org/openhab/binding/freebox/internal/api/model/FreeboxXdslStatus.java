/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
