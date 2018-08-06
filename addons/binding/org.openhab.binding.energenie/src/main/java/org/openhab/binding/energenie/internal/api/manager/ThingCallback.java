/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.manager;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * A {@link ThingCallback} is a callback interface for updating the {@link ThingStatus} of a {@link Thing}.
 *
 * @author Svilen Valkanov - Initial contribution
 *
 */
public interface ThingCallback {

    /**
     * Update the status of a {@link Thing}
     *
     * @param status
     */
    void updateThingStatus(ThingStatus status);

    /**
     * Update the status of a {@link Thing} and the {@link ThingStatusDetail}
     *
     * @param status
     * @param statusDetail
     */
    void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail);

    /**
     * Update the status of a {@link Thing}, the {@link ThingStatusDetail} and the description of the status
     *
     * @param status
     * @param statusDetail
     * @param description
     */
    void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, String description);
}
