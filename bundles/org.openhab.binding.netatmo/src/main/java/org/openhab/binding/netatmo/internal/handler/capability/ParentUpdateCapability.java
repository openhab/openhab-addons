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

import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private final Logger logger = LoggerFactory.getLogger(ParentUpdateCapability.class);

    private @Nullable ScheduledFuture<?> job;

    public ParentUpdateCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    public void initialize() {
        if (job != null) {
            logger.debug("Data update is already requested for '{}'", thingUID);
            return;
        }

        this.job = handler.schedule(() -> {
            logger.debug("Requesting parents data update for Thing '{}'", thingUID);
            if (handler.getBridgeHandler() instanceof CommonInterface bridgeHandler) {
                bridgeHandler.expireData();
            }
            job = null;
        }, RefreshCapability.ASAP);
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        cancelJob();
    }

    private void cancelJob() {
        if (job instanceof ScheduledFuture local) {
            local.cancel(true);
            this.job = null;
        }
    }

    @Override
    public void dispose() {
        cancelJob();
        super.dispose();
    }
}
