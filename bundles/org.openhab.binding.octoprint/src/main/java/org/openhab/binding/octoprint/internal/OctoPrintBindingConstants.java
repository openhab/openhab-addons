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
package org.openhab.binding.octoprint.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OctoPrintBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim-Niclas Ruppert - Initial contribution
 */
@NonNullByDefault
public class OctoPrintBindingConstants {

    private static final String BINDING_ID = "octoprint";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OCTOPRINT = new ThingTypeUID(BINDING_ID, "octoprint");

    // List of all Channel ids
    public static final String PRINT_JOB_STATE = "print_job_state";
    public static final String PRINT_JOB_FILE_NAME = "print_job_file_name";
    public static final String PRINT_JOB_FILE_ORIGIN = "print_job_file_origin";

}
