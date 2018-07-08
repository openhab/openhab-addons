/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
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
package org.openhab.binding.danfossairunit;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DanfossAirUnitBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ralf Duckstein - Initial contribution
 */
public class DanfossAirUnitBindingConstants {

    private static final String BINDING_ID = "danfossairunit";

    // thing type UIDs
    public static final ThingTypeUID THING_TYPE_DANFOSS_AIR_CCM = new ThingTypeUID(BINDING_ID, "danfossairccm");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_DANFOSS_AIR_CCM);

}
