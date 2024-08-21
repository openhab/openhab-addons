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
package org.openhab.binding.kostalinverter.internal.firstgeneration;

import javax.measure.Unit;

/**
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 */
public class ChannelConfig {
    public ChannelConfig(String id, String tag, int num, Unit<?> unit) {
        this.id = id;
        this.tag = tag;
        this.num = num;
        this.unit = unit;
    }

    String id;
    String tag;
    int num;
    Unit<?> unit;
}
