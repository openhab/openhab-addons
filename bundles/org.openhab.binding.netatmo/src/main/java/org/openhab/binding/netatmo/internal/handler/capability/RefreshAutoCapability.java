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
    private static final Duration PROBING_INTERVAL = Duration.ofMinutes(2);

    private final Logger logger = LoggerFactory.getLogger(RefreshAutoCapability.class);

    private Instant dataTimeStamp = Instant.MIN;

    public RefreshAutoCapability(CommonInterface handler) {
        super(handler, Duration.ZERO);
    }

    @Override
    public void expireData() {
        dataTimeStamp = Instant.MIN;
        super.expireData();
    }

    private boolean probing() {
        return dataValidity.getSeconds() <= 0;
    }

    @Override
    protected Duration calcDelay() {
        if (probing()) {
            return PROBING_INTERVAL;
        }

        Duration dataAge = Duration.between(dataTimeStamp, Instant.now());
        if (dataValidity.compareTo(dataAge) > 0) {
            return dataValidity.minus(dataAge).plus(DEFAULT_DELAY);
        }

        logger.debug("Data too old, '{}' going back to probing (data age: {})", thingUID, dataAge);
        dataValidity = Duration.ZERO;
        return PROBING_INTERVAL;
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        super.updateNAThing(newData);
        newData.getLastSeen().map(ZonedDateTime::toInstant).ifPresent(lastSeen -> {
            if (probing()) {
                if (Instant.MIN.equals(dataTimeStamp)) {
                    logger.debug("First data timestamp for '{}' is {}", thingUID, lastSeen);
                } else if (lastSeen.isAfter(dataTimeStamp)) {
                    dataValidity = Duration.between(dataTimeStamp, lastSeen);
                    logger.debug("Data validity period for '{}' identified to be {}", thingUID, dataValidity);
                } else {
                    logger.debug("Data validity period for '{}' not yet found, reference timestamp unchanged",
                            thingUID);
                }
            }
            dataTimeStamp = lastSeen;
        });
    }

    @Override
    protected void afterNewData(@Nullable NAObject newData) {
        properties.put("dataValidity", "%s (probing: %s)".formatted(dataValidity, Boolean.valueOf(probing())));
        super.afterNewData(newData);
    }
}
