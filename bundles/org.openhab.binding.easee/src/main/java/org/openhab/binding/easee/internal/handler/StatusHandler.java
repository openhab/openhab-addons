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
package org.openhab.binding.easee.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * functional interface to provide a function to update status of a thing or bridge.
 *
 * @author Alexander Friese - initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface StatusHandler {
    /**
     * Called from WebInterface#authenticate() to update
     * the thing status because updateStatus is protected.
     *
     * @param status Thing status
     * @param statusDetail Thing status detail
     * @param description Thing status description
     */
    void updateStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description);
}
