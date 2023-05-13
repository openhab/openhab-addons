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
package org.openhab.binding.liquidcheck.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Payload} is used for serializing and deserializing of JSONs.
 * It contains the complete data set within the Measure class, the Expansion class, the Device class, the
 * LiquidCheckSystem class and the wifi class.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Payload {
    public Measure measure = new Measure();
    public Expansion expansion = new Expansion();
    public Device device = new Device();
    public LiquidCheckSystem system = new LiquidCheckSystem();
    public Wifi wifi = new Wifi();
}
