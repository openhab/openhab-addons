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
package org.openhab.binding.liquidcheck.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Raw} is used for serializing and deserializing of JSONs.
 * It contains the raw data measured and calculated by the sensor.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Raw {
    public double level = 0.0000;
    public double content = 0.0000;
}
