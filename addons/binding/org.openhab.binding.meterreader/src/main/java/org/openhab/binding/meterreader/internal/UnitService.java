/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.format.UnitFormat;

import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

import tec.uom.se.spi.DefaultServiceProvider;

/**
 *
 * @author MatthiasS
 *
 */
public class UnitService {

    private static final UnitService INSTANCE = new UnitService();

    private UnitService() {
    }

    public void addDefaultUnits() {
        Unit<?> javaUnit = SmartHomeUnits.WATT.multiply(SmartHomeUnits.HOUR);
        getUnitFormat().label(javaUnit, "Wh");
        getUnitFormat().label(MetricPrefix.KILO(javaUnit), "kWh");

        javaUnit = SmartHomeUnits.AMPERE.multiply(SmartHomeUnits.HOUR);
        getUnitFormat().label(javaUnit, "Ah");
        getUnitFormat().label(MetricPrefix.MILLI(javaUnit), "mAh");
    }

    public UnitFormat getUnitFormat() {
        return new DefaultServiceProvider().getUnitFormatService().getUnitFormat();
    }

    public static UnitService getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Quantity<Q>> Unit<Q> parse(String unit) {
        return (Unit<Q>) getUnitFormat().parse(unit);
    }
}
