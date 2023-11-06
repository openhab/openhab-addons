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
package org.openhab.binding.ftpupload.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FtpUploadBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class FtpUploadBindingConstants {

    public static final String BINDING_ID = "ftpupload";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_IMAGERECEIVER = new ThingTypeUID(BINDING_ID, "imagereceiver");

    // List of all Channel ids
    public static final String IMAGE = "image";
    public static final String IMAGE_RECEIVED_TRIGGER = "image-received";

    // List of all channel parameters
    public static final String PARAM_FILENAME_PATTERN = "filename";

    public static final String EVENT_IMAGE_RECEIVED = "IMAGE_RECEIVED";
}
