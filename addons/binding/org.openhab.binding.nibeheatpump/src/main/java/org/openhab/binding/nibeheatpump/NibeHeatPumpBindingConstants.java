/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NibeHeatPumpBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpBindingConstants {

    private static final String BINDING_ID = "nibeheatpump";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_F1X45_UDP = new ThingTypeUID(BINDING_ID, "f1x45-udp");
    public final static ThingTypeUID THING_TYPE_F1X45_SERIAL = new ThingTypeUID(BINDING_ID, "f1x45-serial");
    public final static ThingTypeUID THING_TYPE_F1X45_SIMULATOR = new ThingTypeUID(BINDING_ID, "f1x45-simulator");

    /**
     * Presents all supported thing types by NibeHeatPump binding.
     */
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_F1X45_UDP, THING_TYPE_F1X45_SERIAL, THING_TYPE_F1X45_SIMULATOR).collect(Collectors.toSet());
}
