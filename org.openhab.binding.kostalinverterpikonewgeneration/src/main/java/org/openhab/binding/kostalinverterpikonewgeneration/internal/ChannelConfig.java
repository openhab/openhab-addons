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
package org.openhab.binding.kostalinverterpikonewgeneration.internal;

import javax.measure.Unit;

/**
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 * @author Örjan Backsell - Added getters and setters (New Generation)
 */
public class ChannelConfig {
    public ChannelConfig(String id, String tag, int num, Unit<?> unit) {
        this.setId(id);
        this.setTag(tag);
        this.setNum(num);
        this.unit = unit;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String id;
    public String tag;
    public int num;
    public Unit<?> unit;
}
