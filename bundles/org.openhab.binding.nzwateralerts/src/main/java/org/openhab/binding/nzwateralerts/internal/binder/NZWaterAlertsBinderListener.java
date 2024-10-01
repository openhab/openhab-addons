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
package org.openhab.binding.nzwateralerts.internal.binder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * # * The {@link NZWaterAlertsBinderListener} is responsible for handling the events from the WebClient and Handler.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public interface NZWaterAlertsBinderListener {
    void updateWaterLevel(int level);

    void updateBindingStatus(ThingStatus thingStatus);

    void updateBindingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String description);
}
