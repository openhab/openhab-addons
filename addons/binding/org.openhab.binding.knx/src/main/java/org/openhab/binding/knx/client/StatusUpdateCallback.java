/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * Callback interface which enables the KNXClient implementations to update the thing status.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public interface StatusUpdateCallback {

    /**
     * see BaseThingHandler
     *
     * @param status
     */
    void updateStatus(ThingStatus status);

    /**
     * see BaseThingHandler
     *
     * @param status
     */
    void updateStatus(ThingStatus status, ThingStatusDetail thingStatusDetail, String message);

}
