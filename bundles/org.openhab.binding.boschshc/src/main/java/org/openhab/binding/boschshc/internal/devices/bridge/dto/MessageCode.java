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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

/**
 * DTO for message codes sent by the Smart Home Controller.
 * <p>
 * JSON Example:
 * 
 * <pre>
 * {
 *   "name": "TILT_DETECTED",
 *   "category": "WARNING"
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 */
public class MessageCode {
    public String name;
    public String category;
}
