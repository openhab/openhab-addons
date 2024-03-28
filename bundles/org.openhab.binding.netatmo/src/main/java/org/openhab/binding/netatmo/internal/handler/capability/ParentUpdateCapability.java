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
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ParentUpdateCapability} is the class used to request data update upon initialization of a module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ParentUpdateCapability extends Capability {
    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(2);

    private final Logger logger = LoggerFactory.getLogger(ParentUpdateCapability.class);
    private Optional<ScheduledFuture<?>> job = Optional.empty();

    public ParentUpdateCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void initialize() {
        job = handler.schedule(() -> {
            logger.debug("Requesting parents data update for Thing '{}'", thingUID);
            CommonInterface bridgeHandler = handler.getBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.expireData();
            }
        }, DEFAULT_DELAY);
    }

    @Override
    public void dispose() {
        job.ifPresent(j -> j.cancel(true));
        job = Optional.empty();
        super.dispose();
    }
}
