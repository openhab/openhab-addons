/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import javax.measure.Unit;

/**
 * The {@link SecondGenerationChannelConfig} class defines methods, which are
 * used in the second generation part of the binding.
 *
 *
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 * @author Örjan Backsell - Added getters and setters (Piko1020, Piko New Generation)
 */

public class SecondGenerationChannelConfig {
    public SecondGenerationChannelConfig(String id, String tag, int num, Unit<?> unit) {
        this.id = id;
        this.tag = tag;
        this.num = num;
        this.unit = unit;
    }

    public String id;
    public String tag;
    public int num;
    public Unit<?> unit;
}
