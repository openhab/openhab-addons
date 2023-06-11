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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author Markus Pfleger - Initial contribution
 */
public enum State {
    UNKNOWN,
    NOT_APPLICABLE,
    PAUSED,
    IN_OPERATION,
    WAIT_UPDATING,
    WAIT_POWER_UP,
    RESTRICTED,
    OFF,
    STOPPED,
    ERROR,
    FATAL_ERROR,
    ERROR_AT_POWER_UP
}
