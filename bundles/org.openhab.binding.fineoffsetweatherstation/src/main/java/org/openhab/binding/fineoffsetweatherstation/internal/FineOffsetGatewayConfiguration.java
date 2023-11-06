/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Protocol;

/**
 * The {@link FineOffsetGatewayConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetGatewayConfiguration {

    public static final String IP = "ip";
    public static final String PORT = "port";

    public static final String PROTOCOL = "protocol";

    public @Nullable String ip;
    public int port = 45000;
    public int pollingInterval = 16;
    public int discoverInterval = 900;

    public Protocol protocol = Protocol.DEFAULT;
}
