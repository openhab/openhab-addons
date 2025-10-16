/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.handler;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.actions.AVMFritzPowerMeterActions;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link AVMFritzPowerMeterActionsHandler} defines interface handlers to handle power meter thing actions.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public interface AVMFritzPowerMeterActionsHandler extends ThingHandler {

    @Override
    default Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AVMFritzPowerMeterActions.class);
    }

    /**
     * Enables high refresh polling for this power meter.
     *
     * @param deviceId Id of the device.
     */
    void enablePowerMeterHighRefresh(long deviceId);

    /**
     * Disables high refresh polling for this power meter.
     */
    void disablePowerMeterHighRefresh();
}
