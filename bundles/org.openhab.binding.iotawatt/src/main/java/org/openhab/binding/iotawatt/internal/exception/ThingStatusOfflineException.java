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
package org.openhab.binding.iotawatt.internal.exception;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Exception to change the Thing status to offline.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class ThingStatusOfflineException extends Exception {
    public final ThingStatusDetail thingStatusDetail;
    @Nullable
    public final String description;

    public ThingStatusOfflineException(@NonNull ThingStatusDetail statusDetail) {
        this.thingStatusDetail = statusDetail;
        this.description = null;
    }

    public ThingStatusOfflineException(@NonNull ThingStatusDetail statusDetail, @Nullable String description) {
        this.thingStatusDetail = statusDetail;
        this.description = description;
    }
}
