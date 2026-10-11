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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;

/**
 * Selectors for the fields of the packed wallbox command.
 *
 * <p>
 * The wallbox is not configured through individual RSCP tags: sun mode, charging current and the
 * suspend toggle are fields of one byte array sent as {@code WB_EXTERN_DATA}. Two of those fields have
 * no tag of their own in the RSCP interface, so they are identified by the private constants here. The
 * values are deliberately outside the range the device uses.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
final class E3DCWallboxField {

    /**
     * Selects the charging mode byte of the packed command. Bit 7 of the wallbox status byte carries
     * the same information, which is where the value is read from.
     */
    static final int SUN_MODE = 0x7F000001;

    /**
     * Selects the suspend toggle byte of the packed command. The device reports the resulting state in
     * bit 6 of the wallbox status byte and interprets this field as a request to change it.
     */
    static final int SUSPENDED = 0x7F000002;

    /** The tag that carries the packed command itself. */
    static final int EXTERN_DATA = RscpTag.WB_EXTERN_DATA;

    private E3DCWallboxField() {
    }
}
