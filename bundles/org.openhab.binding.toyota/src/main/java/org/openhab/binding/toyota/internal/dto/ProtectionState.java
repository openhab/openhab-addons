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
package org.openhab.binding.toyota.internal.dto;

import java.time.ZonedDateTime;

/**
 * This class holds various data regarding car protection status
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ProtectionState {
    public enum OverallStatus {
        NOK,
        OK,
        UNKNOWN;
    }

    public Doors doors;
    public Hood hood;
    public Lamps lamps;
    public Windows windows;
    public Lock lock;
    public Key key;

    public OverallStatus overallStatus;
    public ZonedDateTime timestamp;
}
