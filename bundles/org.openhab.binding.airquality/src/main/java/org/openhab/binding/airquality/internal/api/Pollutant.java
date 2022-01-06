/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.api;

import static org.openhab.binding.airquality.internal.api.Index.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Pollutant} enum lists all measures
 * of the AQI Level associated with their standard color.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Pollutant {
    PM25(Units.MICROGRAM_PER_CUBICMETRE, 1,
            Set.of(SensitiveGroup.RESPIRATORY, SensitiveGroup.HEART, SensitiveGroup.ELDERLY, SensitiveGroup.CHILDREN),
            new ConcentrationRange(0, 12, ZERO), new ConcentrationRange(12.1, 35.4, FIFTY),
            new ConcentrationRange(35.5, 55.4, ONE_HUNDRED), new ConcentrationRange(55.5, 150.4, ONE_HUNDRED_FIFTY),
            new ConcentrationRange(150.5, 250.4, TWO_HUNDRED), new ConcentrationRange(250.5, 350.4, THREE_HUNDRED),
            new ConcentrationRange(350.5, 500.4, FOUR_HUNDRED)),
    PM10(Units.MICROGRAM_PER_CUBICMETRE, 0, Set.of(SensitiveGroup.RESPIRATORY), new ConcentrationRange(0, 54, ZERO),
            new ConcentrationRange(55, 154, FIFTY), new ConcentrationRange(155, 254, ONE_HUNDRED),
            new ConcentrationRange(255, 354, ONE_HUNDRED_FIFTY), new ConcentrationRange(355, 424, TWO_HUNDRED),
            new ConcentrationRange(425, 504, THREE_HUNDRED), new ConcentrationRange(505, 604, FOUR_HUNDRED)),
    NO2(Units.PARTS_PER_BILLION, 0,
            Set.of(SensitiveGroup.ASTHMA, SensitiveGroup.RESPIRATORY, SensitiveGroup.ELDERLY, SensitiveGroup.CHILDREN),
            new ConcentrationRange(0, 53, ZERO), new ConcentrationRange(54, 100, FIFTY),
            new ConcentrationRange(101, 360, ONE_HUNDRED), new ConcentrationRange(361, 649, ONE_HUNDRED_FIFTY),
            new ConcentrationRange(650, 1249, TWO_HUNDRED), new ConcentrationRange(1250, 1649, THREE_HUNDRED),
            new ConcentrationRange(1650, 2049, FOUR_HUNDRED)),
    SO2(Units.PARTS_PER_BILLION, 0, Set.of(SensitiveGroup.ASTHMA), new ConcentrationRange(0, 35, ZERO),
            new ConcentrationRange(36, 75, FIFTY), new ConcentrationRange(76, 185, ONE_HUNDRED),
            new ConcentrationRange(186, 304, ONE_HUNDRED_FIFTY), new ConcentrationRange(305, 604, TWO_HUNDRED),
            new ConcentrationRange(605, 804, THREE_HUNDRED), new ConcentrationRange(805, 1004, FOUR_HUNDRED)),
    CO(Units.PARTS_PER_BILLION, 1, Set.of(SensitiveGroup.HEART), new ConcentrationRange(0, 4.4, ZERO),
            new ConcentrationRange(4.5, 9.4, FIFTY), new ConcentrationRange(9.5, 12.4, ONE_HUNDRED),
            new ConcentrationRange(12.5, 15.4, ONE_HUNDRED_FIFTY), new ConcentrationRange(15.5, 30.4, TWO_HUNDRED),
            new ConcentrationRange(30.5, 40.4, THREE_HUNDRED), new ConcentrationRange(40.5, 50.4, FOUR_HUNDRED)),
    O3(Units.PARTS_PER_BILLION, 3, Set.of(SensitiveGroup.CHILDREN, SensitiveGroup.ASTHMA),
            new ConcentrationRange(0, 54, ZERO), new ConcentrationRange(55, 124, FIFTY),
            new ConcentrationRange(125, 164, ONE_HUNDRED), new ConcentrationRange(165, 204, ONE_HUNDRED_FIFTY),
            new ConcentrationRange(205, 404, TWO_HUNDRED), new ConcentrationRange(405, 504, THREE_HUNDRED),
            new ConcentrationRange(505, 604, FOUR_HUNDRED));

    public static enum SensitiveGroup {
        RESPIRATORY,
        HEART,
        ELDERLY,
        CHILDREN,
        ASTHMA;
    }

    public final Set<SensitiveGroup> sensitiveGroups;
    private final Unit<?> unit;
    private final Set<ConcentrationRange> breakpoints;
    private final int scale;

    Pollutant(Unit<?> unit, int scale, Set<SensitiveGroup> groups, ConcentrationRange... concentrations) {
        this.sensitiveGroups = groups;
        this.unit = unit;
        this.breakpoints = Set.of(concentrations);
        this.scale = scale;
    }

    public State toQuantity(double idx) {
        for (ConcentrationRange concentration : breakpoints) {
            double equivalent = concentration.getConcentration(idx);
            if (equivalent != -1) {
                return new QuantityType<>(BigDecimal.valueOf(equivalent).setScale(scale, RoundingMode.HALF_UP), unit);
            }
        }
        return UnDefType.UNDEF;
    }
}
