/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gruenbecksoftener;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SoftenerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault
public class SoftenerBindingConstants {

    public static final String BINDING_ID = "gruenbecksoftener";
    public static final String LOCAL = "local";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SOFTENER = new ThingTypeUID(BINDING_ID, "softener");

    // List of all Channel id's

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SOFTENER);

}
