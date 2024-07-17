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

    private Instant dataTimeStamp = Instant.MIN;

    public RefreshAutoCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void expireData() {
        dataTimeStamp = Instant.MIN;
        super.expireData();
    }

    @Override
    protected Duration calcDelay() {
        if (Instant.MIN.equals(dataTimeStamp)) {
            return PROBING_INTERVAL;
        }

        Duration dataAge = Duration.between(dataTimeStamp, Instant.now());

        Duration delay = dataValidity.minus(dataAge);
        if (delay.isNegative() || delay.isZero()) {
            logger.debug("{} did not update data in expected time, return to probing", thingUID);
            dataTimeStamp = Instant.MIN;
            return PROBING_INTERVAL;
        }

        return delay.plus(DEFAULT_DELAY);
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        super.updateNAThing(newData);
        ZonedDateTime lastSeen = newData.getLastSeen();
        dataTimeStamp = lastSeen != null ? lastSeen.toInstant() : Instant.MIN;
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        properties.put("probing", Boolean.valueOf(Instant.MIN.equals(dataTimeStamp)).toString());
        super.afterNewData(newData);
    }
}
