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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.RestManager;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CacheCapability} give the ability to buffer RestManager related requests and reduce server requests
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class CacheCapability<T extends RestManager> extends RestCapability<T> {
    private final Logger logger = LoggerFactory.getLogger(CacheCapability.class);
    private final Duration validity;

    private List<NAObject> lastResult = List.of();
    private Instant requestTS = Instant.MIN;

    public CacheCapability(CommonInterface handler, Duration validity, Class<T> restManagerClazz) {
        super(handler, restManagerClazz);
        this.validity = validity;
    }

    @Override
    protected synchronized List<NAObject> updateReadings(T api) {
        Instant now = Instant.now();

        if (!stillValid(now)) {
            logger.debug("{} requesting fresh data for {}", getClass().getSimpleName(), thingUID);
            List<NAObject> result = getFreshData(api);
            if (!result.isEmpty()) {
                lastResult = result;
                requestTS = now;
            }
        }

        return lastResult;
    }

    protected boolean stillValid(Instant ts) {
        return requestTS.plus(validity).isAfter(ts);
    }

    protected abstract List<NAObject> getFreshData(T api);
}
