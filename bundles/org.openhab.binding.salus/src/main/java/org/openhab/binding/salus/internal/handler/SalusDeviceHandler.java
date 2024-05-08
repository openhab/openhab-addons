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
package org.openhab.binding.salus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * Wrapper for ThingHandler that exposes updateStatus
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public interface SalusDeviceHandler {
    /**
     * Exposes {@link org.openhab.core.thing.binding.ThingHandler#handleCommand(ChannelUID, Command)}
     */
    void handleCommand(ChannelUID channelUID, Command command);

    /**
     * Exposes
     * {@link org.openhab.core.thing.binding.BaseThingHandler#updateStatus(ThingStatus, ThingStatusDetail, String)}
     */
    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);
}
