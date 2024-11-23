/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 * Used for deserializing the XML response of the LCN-PCHK discovery protocol.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "content" })
public class ExtService {
    private final int localPort;
    @SuppressWarnings("unused")
    private final String content = "";

    public ExtService(int localPort) {
        this.localPort = localPort;
    }

    public int getLocalPort() {
        return localPort;
    }
}
