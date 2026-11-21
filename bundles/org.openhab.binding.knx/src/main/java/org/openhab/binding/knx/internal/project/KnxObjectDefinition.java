/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Whether the manufacturer files of the project define a group object, and if not, where the way to the
 * definition ended.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public enum KnxObjectDefinition {
    /** The object reference and the object were found in the application program. */
    FOUND,
    /** No {@code Hardware2Program} of the device was found in the manufacturer's {@code Hardware.xml}. */
    NO_HARDWARE,
    /** The device's application program file is not in the project file, or it holds another program. */
    NO_PROGRAM_FILE,
    /** The application program does not define the object reference or the object it refers to. */
    NOT_IN_PROGRAM
}
