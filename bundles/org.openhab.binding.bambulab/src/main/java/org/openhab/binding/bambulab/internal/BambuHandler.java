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
package org.openhab.binding.bambulab.internal;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
import org.slf4j.Logger;

/**
 * This interface is to decouple {@link PrinterHandler} and {@link Camera}
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public interface BambuHandler {
    /**
     * Same as
     * {@link org.openhab.core.thing.binding.BaseThingHandler#updateStatus(ThingStatusDetail, String)}
     *
     * @see org.openhab.core.thing.binding.BaseThingHandler#updateStatus(ThingStatusDetail, String)
     */
    void updateStatus(ThingStatusDetail statusDetail, @Nullable String description);

    /**
     * Same as
     * {@link org.openhab.core.thing.binding.BaseThingHandler#updateState(org.openhab.core.thing.ChannelUID, State)}
     *
     * @see org.openhab.core.thing.binding.BaseThingHandler#updateState(org.openhab.core.thing.ChannelUID, State)
     */
    void updateState(String channelUID, State state);

    /**
     * Same as {@link org.openhab.core.thing.binding.BaseThingHandler#updateStatus(ThingStatus)}
     *
     * @see org.openhab.core.thing.binding.BaseThingHandler#updateStatus(ThingStatus)
     */
    void updateStatus(ThingStatus status);

    /**
     * Returns current logger
     *
     * @return logger
     */
    Logger getLogger();

    /**
     * Returns current scheduler
     *
     * @return scheduler
     */
    ScheduledExecutorService getScheduler();
}
