/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Callback interface which enables the KNXClient implementations to update the thing status.
 *
 * @author Simon Kaufmann - Initial contribution
 *
 */
@NonNullByDefault
public interface StatusUpdateCallback {

    /**
     * Updates the status of the thing.
     *
     * see {@link org.openhab.core.thing.binding.BaseThingHandler}
     *
     * @param status the status
     */
    void updateStatus(ThingStatus status);

    /**
     * Updates the status of the thing.
     *
     * see {@link org.openhab.core.thing.binding.BaseThingHandler}
     * 
     * @param status the status
     * @param statusDetail the detail of the status
     * @param description the description of the status
     */
    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description);
}
