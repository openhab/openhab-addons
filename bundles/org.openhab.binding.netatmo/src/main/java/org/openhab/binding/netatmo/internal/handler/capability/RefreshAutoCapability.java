/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RefreshAutoCapability} implements probing and auto-adjusting refresh strategy
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RefreshAutoCapability extends RefreshCapability {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(15);

    private final Logger logger = LoggerFactory.getLogger(RefreshAutoCapability.class);

    private @Nullable Instant dataTimestamp = null;

    public RefreshAutoCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void expireData() {
        dataTimestamp = null;
        super.expireData();
    }

    @Override
    protected Duration calcDelay() {
        Instant timestamp = dataTimestamp;
        if (timestamp == null) {
            return PROBING_INTERVAL;
        }

        Duration dataAge = Duration.between(timestamp, Instant.now());
        Duration delay = dataValidity.minus(dataAge);

        if (delay.isPositive()) {
            return delay.plus(DEFAULT_DELAY);
        }

        logger.debug("{} did not update data in expected time, return to probing", thingUID);
        dataTimestamp = null;
        return PROBING_INTERVAL;
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        super.updateNAThing(newData);
        dataTimestamp = newData.getLastSeen() instanceof ZonedDateTime ls ? ls.toInstant() : null;
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        properties.put("probing", Boolean.valueOf(dataTimestamp == null).toString());
        super.afterNewData(newData);
    }
}
