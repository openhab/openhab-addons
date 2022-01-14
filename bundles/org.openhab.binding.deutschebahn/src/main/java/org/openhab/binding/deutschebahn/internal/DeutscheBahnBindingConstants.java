/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DeutscheBahnBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class DeutscheBahnBindingConstants {

    /**
     * Binding-ID.
     */
    public static final String BINDING_ID = "deutschebahn";

    /**
     * {@link ThingTypeUID} for Timetable-API Bridge.
     */
    public static final ThingTypeUID TIMETABLE_TYPE = new ThingTypeUID(BINDING_ID, "timetable");

    /**
     * {@link ThingTypeUID} for Train.
     */
    public static final ThingTypeUID TRAIN_TYPE = new ThingTypeUID(BINDING_ID, "train");
}
