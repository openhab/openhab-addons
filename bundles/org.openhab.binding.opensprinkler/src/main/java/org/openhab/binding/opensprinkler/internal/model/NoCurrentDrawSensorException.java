/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.model;

/**
 * Indicates, that a device is missing a sensor to measure the current draw of itself.
 *
 * @author Florian Schmidt - Initial contribution
 */
public class NoCurrentDrawSensorException extends Exception {
    private static final long serialVersionUID = 2251925316743442346L;
}
