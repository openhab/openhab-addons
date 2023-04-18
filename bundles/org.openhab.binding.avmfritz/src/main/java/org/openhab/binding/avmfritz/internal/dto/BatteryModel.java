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
package org.openhab.binding.avmfritz.internal.dto;

import java.math.BigDecimal;

/**
 * See {@link AVMFritzBaseModel} -> {@link DeviceModel} and {@link HeatingModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public interface BatteryModel {

    static final BigDecimal BATTERY_OFF = BigDecimal.ZERO;
    static final BigDecimal BATTERY_ON = BigDecimal.ONE;

    BigDecimal getBattery();

    BigDecimal getBatterylow();
}
