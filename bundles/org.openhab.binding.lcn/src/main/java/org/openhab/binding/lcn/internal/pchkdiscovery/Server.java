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
package org.openhab.binding.lcn.internal.pchkdiscovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * Used for deserializing the XML response of the LCN-PCHK discovery protocol.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "content" })
public class Server {
    @XStreamAsAttribute
    private final int requestId;
    @XStreamAsAttribute
    private final String machineId;
    @XStreamAsAttribute
    private final String machineName;
    @XStreamAsAttribute
    private final String osShort;
    @XStreamAsAttribute
    private final String osLong;
    private final String content;

    public Server(int requestId, String machineId, String machineName, String osShort, String osLong, String content) {
        this.requestId = requestId;
        this.machineId = machineId;
        this.machineName = machineName;
        this.osShort = osShort;
        this.osLong = osLong;
        this.content = content;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getOsShort() {
        return osShort;
    }

    public String getOsLong() {
        return osLong;
    }

    public String getContent() {
        return content;
    }

    public Object getMachineName() {
        return machineName;
    }
}
