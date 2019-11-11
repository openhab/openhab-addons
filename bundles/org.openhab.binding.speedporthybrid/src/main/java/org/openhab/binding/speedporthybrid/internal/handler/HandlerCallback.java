/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedporthybrid.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * Provide a callback function for {@link ThingStatus} updates.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public interface HandlerCallback {

    /**
     * Update the status of the callback implementor.
     *
     * @param status the new {@link ThingStatus}.
     * @param detail the new {@link ThingStatusDetail}.
     * @param description the optional description.
     */
    void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description);

}
