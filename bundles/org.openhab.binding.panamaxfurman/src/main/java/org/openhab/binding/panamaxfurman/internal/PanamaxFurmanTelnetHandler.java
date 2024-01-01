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
package org.openhab.binding.panamaxfurman.internal;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.transport.PanamaxFurmanDeviceUnavailableException;
import org.openhab.binding.panamaxfurman.internal.transport.PanmaxFurmanConnector;
import org.openhab.binding.panamaxfurman.internal.transport.TelnetAndRs232ProtocolMapper;
import org.openhab.binding.panamaxfurman.internal.util.TimeInterval;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Contains fields mapping thing configuration parameters.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanamaxFurmanTelnetHandler extends PanamaxFurmanAbstractHandler implements ThingHandler {
    protected final PanamaxFurmanTelnetConfiguration config;
    private final String connectionName;

    public PanamaxFurmanTelnetHandler(Thing thing) {
        super(thing, new TelnetAndRs232ProtocolMapper(thing));
        this.config = getConfig().as(PanamaxFurmanTelnetConfiguration.class);
        this.connectionName = config.getAddress() + ":" + config.getTelnetPort();
    }

    @Override
    protected @Nullable PanmaxFurmanConnector buildConnector(Configuration genericConfig) {
        // Set our read timeout to 3x the poll interval. That way, if the unit doesn't respond after 2 poll attempts,
        // the connection will automatically be closed and reopened
        int socketReadTimeoutInSeconds = (int) pollInterval().unit().toSeconds(pollInterval().duration() * 3);
        try {
            return new PanmaxFurmanConnector(connectionName, config.getAddress(), config.getTelnetPort(),
                    socketReadTimeoutInSeconds, this);
        } catch (RuntimeException e) {
            // RuntimeExcpetions are unexpected, propagate
            throw e;
        } catch (PanamaxFurmanDeviceUnavailableException e) {
            // Nothing to log - error detail was logged in the in the PanmaxFurmanConnector constructor
            return null;
        }
    }

    @Override
    protected String getConnectionName() {
        return connectionName;
    }

    @Override
    protected TimeInterval pollInterval() {
        // The telnet protocol supports push notifications, so technically we shouldn't need to poll... but it's the
        // only way to ensure we stay connected. Use a relatively long value
        return new TimeInterval(30, TimeUnit.SECONDS);
    }
}
