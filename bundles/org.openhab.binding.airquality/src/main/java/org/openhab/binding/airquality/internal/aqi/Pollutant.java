package org.openhab.binding.airquality.internal.aqi;

import static org.openhab.binding.airquality.internal.aqi.Index.*;
import static org.openhab.binding.airquality.internal.aqi.SensitiveGroups.*;

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
 * List of pollutant codes and properties.
 */
@NonNullByDefault
public enum Pollutant {
    PM25(Set.of(RESPIRATORY, HEART, ELDERLY, CHILDREN), Units.MICROGRAM_PER_CUBICMETRE, 1,
            Set.of(new Concentration(0, 12, ZERO), new Concentration(12.1, 35.4, FIFTY),
                    new Concentration(35.5, 55.4, ONE_HUNDRED), new Concentration(55.5, 150.4, ONE_HUNDRED_FIFTY),
                    new Concentration(150.5, 250.4, TWO_HUNDRED), new Concentration(250.5, 350.4, THREE_HUNDRED),
                    new Concentration(350.5, 500.4, FOUR_HUNDRED))),
    PM10(Set.of(RESPIRATORY), Units.MICROGRAM_PER_CUBICMETRE, 0,
            Set.of(new Concentration(0, 54, ZERO), new Concentration(55, 154, FIFTY),
                    new Concentration(155, 254, ONE_HUNDRED), new Concentration(255, 354, ONE_HUNDRED_FIFTY),
                    new Concentration(355, 424, TWO_HUNDRED), new Concentration(425, 504, THREE_HUNDRED),
                    new Concentration(505, 604, FOUR_HUNDRED))),
    NO2(Set.of(ASTHMA, RESPIRATORY, ELDERLY, CHILDREN), Units.PARTS_PER_BILLION, 0,
            Set.of(new Concentration(0, 53, ZERO), new Concentration(54, 100, FIFTY),
                    new Concentration(101, 360, ONE_HUNDRED), new Concentration(361, 649, ONE_HUNDRED_FIFTY),
                    new Concentration(650, 1249, TWO_HUNDRED), new Concentration(1250, 1649, THREE_HUNDRED),
                    new Concentration(1650, 2049, FOUR_HUNDRED))),
    SO2(Set.of(ASTHMA), Units.PARTS_PER_BILLION, 0,
            Set.of(new Concentration(0, 35, ZERO), new Concentration(36, 75, FIFTY),
                    new Concentration(76, 185, ONE_HUNDRED), new Concentration(186, 304, ONE_HUNDRED_FIFTY),
                    new Concentration(305, 604, TWO_HUNDRED), new Concentration(605, 804, THREE_HUNDRED),
                    new Concentration(805, 1004, FOUR_HUNDRED))),
    CO(Set.of(HEART), Units.PARTS_PER_BILLION, 1,
            Set.of(new Concentration(0, 4.4, ZERO), new Concentration(4.5, 9.4, FIFTY),
                    new Concentration(9.5, 12.4, ONE_HUNDRED), new Concentration(12.5, 15.4, ONE_HUNDRED_FIFTY),
                    new Concentration(15.5, 30.4, TWO_HUNDRED), new Concentration(30.5, 40.4, THREE_HUNDRED),
                    new Concentration(40.5, 50.4, FOUR_HUNDRED))),
    O3(Set.of(CHILDREN, ASTHMA), Units.PARTS_PER_BILLION, 3,
            Set.of(new Concentration(0, 54, ZERO), new Concentration(55, 124, FIFTY),
                    new Concentration(125, 164, ONE_HUNDRED), new Concentration(165, 204, ONE_HUNDRED_FIFTY),
                    new Concentration(205, 404, TWO_HUNDRED), new Concentration(405, 504, THREE_HUNDRED),
                    new Concentration(505, 604, FOUR_HUNDRED)));

    private final Set<SensitiveGroups> sensitiveGroups;
    private final Unit<?> unit;
    private final Set<Concentration> breakpoints;
    private final int scale;

    Pollutant(Set<SensitiveGroups> groups, Unit<?> unit, int scale, Set<Concentration> concentrations) {
        this.sensitiveGroups = groups;
        this.unit = unit;
        this.breakpoints = concentrations;
        this.scale = scale;
    }

    public Set<Concentration> getBreakpoints() {
        return breakpoints;
    }

    public Set<SensitiveGroups> getSensitiveGroups() {
        return sensitiveGroups;
    }

    public State toQuantity(double idx) {
        for (Concentration concentration : breakpoints) {
            if (concentration.getIndex().contains(idx)) {
                BigDecimal quantity = BigDecimal.valueOf(concentration.getSpan() / concentration.getIndex().getSpan()
                        * (idx - concentration.getIndex().getMin()) + concentration.getMin());
                quantity = quantity.setScale(scale, RoundingMode.HALF_UP);
                return new QuantityType<>(quantity, unit);
            }
        }
        return UnDefType.UNDEF;
    }
}
