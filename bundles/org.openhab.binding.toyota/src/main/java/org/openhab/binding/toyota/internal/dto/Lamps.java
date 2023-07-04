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

/**
 * This class holds the status of all the car lamps
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class Lamps {
    public boolean warning;
    public Lamp headLamp;
    public Lamp tailLamp;
    public Lamp hazardLamp;
}
