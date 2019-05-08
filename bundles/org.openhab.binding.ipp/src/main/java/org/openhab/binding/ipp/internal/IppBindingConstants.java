/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.ipp.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IppBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tobias Braeutigam - Initial contribution
 */
@NonNullByDefault
public class IppBindingConstants {

    public static final String BINDING_ID = "ipp";

    // List of all Thing Type UIDs
    public static final ThingTypeUID PRINTER_THING_TYPE = new ThingTypeUID(BINDING_ID, "printer");

    // List of all Channel ids
    public static final String JOBS_CHANNEL = "jobs";
    public static final String WAITING_JOBS_CHANNEL = "waitingJobs";
    public static final String DONE_JOBS_CHANNEL = "doneJobs";

    public static final String PRINTER_PARAMETER_URL = "url";
    public static final String PRINTER_PARAMETER_NAME = "name";
    public static final String PRINTER_PARAMETER_REFRESH_INTERVAL = "refresh";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(PRINTER_THING_TYPE);
}
