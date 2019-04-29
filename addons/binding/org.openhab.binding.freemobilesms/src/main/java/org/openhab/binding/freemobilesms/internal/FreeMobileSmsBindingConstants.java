/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freemobilesms.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FreeMobileSmsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Guilhem Bonnefille <guilhem.bonnefille@gmail.com> - Initial contribution
 */
@NonNullByDefault
public class FreeMobileSmsBindingConstants {

    private static final String BINDING_ID = "freemobilesms";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    // List of all Channel ids
    public static final String CHANNEL_MESSAGE = "message";
}
