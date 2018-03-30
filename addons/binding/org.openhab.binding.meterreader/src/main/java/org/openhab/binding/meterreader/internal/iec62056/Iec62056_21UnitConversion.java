/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.iec62056;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.openhab.binding.meterreader.internal.UnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MatthiasS
 *
 */
public class Iec62056_21UnitConversion {

    private final static Logger logger = LoggerFactory.getLogger(Iec62056_21UnitConversion.class);

    @SuppressWarnings("unchecked")
    public static <Q extends Quantity<Q>> Unit<Q> getUnit(String unit) {
        if (unit != null && !unit.isEmpty()) {
            try {
                return (Unit<Q>) UnitService.getInstance().parse(unit);
            } catch (Exception e) {
                logger.warn("Failed to parse unit {}: {}", unit, e.getMessage());
                return null;
            }
        }
        return null;
    }

}
