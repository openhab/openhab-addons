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
package org.openhab.binding.folding.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FoldingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
@NonNullByDefault
public class FoldingBindingConstants {

    public static final String BINDING_ID = "folding";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CLIENT = new ThingTypeUID(BINDING_ID, "client");
    public static final ThingTypeUID THING_TYPE_SLOT = new ThingTypeUID(BINDING_ID, "slot");

    public static final String PARAM_SLOT_ID = "id";

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
}
