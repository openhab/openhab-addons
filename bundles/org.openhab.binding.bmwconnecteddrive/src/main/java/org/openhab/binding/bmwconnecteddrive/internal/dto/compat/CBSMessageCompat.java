/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.compat;

/**
 * The {@link CBSMessageCompat} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CBSMessageCompat {
    public String description; // "Nächster Wechsel spätestens zum angegebenen Termin.",
    public String text; // "Bremsflüssigkeit",
    public int id; // 3,
    public String status; // "OK",
    public String messageType; // "CBS",
    public String date; // "2021-11"
    public int unitOfLengthRemaining; // ": "2000"
}
