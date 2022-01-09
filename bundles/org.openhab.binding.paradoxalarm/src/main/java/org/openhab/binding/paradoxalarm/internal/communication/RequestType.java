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
package org.openhab.binding.paradoxalarm.internal.communication;

/**
 * The {@link RequestType}. Enum with possible request types to Paradox system.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public enum RequestType {
    LOGON_SEQUENCE,
    RAM,
    EPROM,
    PARTITION_COMMAND
}
