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
package org.openhab.binding.argoclima.internal.device.api.protocol.elements;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The element reporting current time to the device
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class CurrentTimeParam extends ArgoApiElementBase {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     */
    public CurrentTimeParam(IArgoSettingProvider settingsProvider) {
        super(settingsProvider);
    }

    private static ZonedDateTime utcNow() {
        return ZonedDateTime.now(Objects.requireNonNull(ZoneId.of("UTC")));
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This element doesn't really get any device-side commands
     */
    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        logger.debug("Got state: {} for a parameter that doesn't support it!", responseValue);
    }

    @Override
    public State toState() {
        return new DateTimeType(utcNow());
    }

    /**
     * {@inheritDoc}
     * <p>
     * The current time is always sent
     */
    @Override
    public boolean isAlwaysSent() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Specialized implementation, always providing latest *now* value
     */
    @Override
    public String getDeviceApiValue() {
        var t = utcNow();
        return Integer.toString(TimeParam.fromHhMm(t.getHour(), t.getMinute()));
    }

    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        logger.debug("Got command for a parameter that doesn't support it!");
        return HandleCommandResult.rejected(); // Does not handle any commands
    }
}
