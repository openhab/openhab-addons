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
package org.openhab.binding.anel.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AnelConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelConfiguration {

    public @Nullable String hostname;
    public @Nullable String user;
    public @Nullable String password;
    /** Port to send data from openhab to device. */
    public int udpSendPort = IAnelConstants.DEFAULT_SEND_PORT;
    /** Openhab receives messages via this port from device. */
    public int udpReceivePort = IAnelConstants.DEFAULT_RECEIVE_PORT;

    public AnelConfiguration() {
    }

    public AnelConfiguration(@Nullable String hostname, @Nullable String user, @Nullable String password, int sendPort,
            int receivePort) {
        this.hostname = hostname;
        this.user = user;
        this.password = password;
        this.udpSendPort = sendPort;
        this.udpReceivePort = receivePort;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[hostname=");
        builder.append(hostname);
        builder.append(",user=");
        builder.append(user);
        builder.append(",password=");
        builder.append(mask(password));
        builder.append(",udpSendPort=");
        builder.append(udpSendPort);
        builder.append(",udpReceivePort=");
        builder.append(udpReceivePort);
        builder.append("]");
        return builder.toString();
    }

    private @Nullable String mask(@Nullable String string) {
        return string == null ? null : string.replaceAll(".", "X");
    }
}
