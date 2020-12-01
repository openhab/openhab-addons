/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.providers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This interface should be implemented by any listener of model changes (ie new thing types for a specific model). The
 * {@link #thingTypeFound(ThingTypeUID)} will be called back when a new thing type is found for the related model.
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonyModelListener {
    /**
     * The call back when a new thing type is found
     * 
     * @param uid a non-null thing type uid
     */
    void thingTypeFound(ThingTypeUID uid);
}
