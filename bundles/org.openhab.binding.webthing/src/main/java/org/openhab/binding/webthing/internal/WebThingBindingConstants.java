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
package org.openhab.binding.webthing.internal;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WebThingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class WebThingBindingConstants {

    public static final String BINDING_ID = "webthing";

    public static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "generic");

    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(WebThingBindingConstants.THING_TYPE_UID);

    public static final String MDNS_SERVICE_TYPE = "_webthing._tcp.local.";
}
