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
package org.openhab.binding.senseenergy.internal.handler.helpers;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.measure.Unit;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * @link { SenseEnergyPowerLevels }
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyPowerLevels {
    public NavigableMap<String, @Nullable QuantityType<Power>> powerStateLevels = Collections.emptyNavigableMap();
    public List<@Nullable QuantityType<Power>> powerValueLevels = Collections.emptyList();

    /*
     * will parse out any single value levels
     */
    @SuppressWarnings("unchecked") // prevent warning in cast to QuantityType<Power> which is guaranteed by specifying
                                   // the Units.WATT
    public void parse(String levels) {
        String[] splitList = levels.split(",", 0);
        final Pattern p = Pattern.compile("\\p{Space}*(\\p{Alpha}\\p{Alnum}*)\\p{Space}*=(.+)");

        // @formatter:off
        // extract non-state specified power levels
        powerValueLevels = Arrays.stream(splitList).filter(s -> s.indexOf('=') == -1)
                .map(s -> (QuantityType<Power>) parseQuantityType(s, Units.WATT))
                .sorted()
                .collect(Collectors.toList());

        // extract state specified power levels
        powerStateLevels = Arrays.stream(splitList)
                .filter(s -> s.indexOf('=') != -1)      // "OFF=0, LOW=2W, HIGH=5W", 0, 3);
                .map(s -> { Matcher m = p.matcher(s); m.matches(); return m; })
                .collect(Collectors.toMap(m -> m.group(1).toUpperCase(), m -> (QuantityType<Power>) parseQuantityType(m.group(2), Units.WATT), (m1, m2) -> m1, TreeMap::new));
        // @formatter:on
    }

    public int getNumValueLevels() {
        return powerValueLevels.size();
    }

    public int getNumStateLevels() {
        return powerStateLevels.size();
    }

    @SuppressWarnings("null")
    @Override
    public String toString() {
        String output = "";

        // @formatter:off
        if (!powerValueLevels.isEmpty()) {
            output = powerValueLevels.stream()
                    .filter(Objects::nonNull)
                    .map(qt -> qt.toString())
                    .collect(Collectors.joining(","));
        }

        if (!powerStateLevels.isEmpty()) {
            output += (output.isEmpty()) ? "" : ",";

            output += powerStateLevels.descendingMap().entrySet().stream()
                    .filter(kv -> kv.getValue() != null)
                    .map(kv -> kv.getKey() + "=" + kv.getValue().toString())
                    .collect(Collectors.joining(","));
        }
        // @formatter:on

        return output;
    }

    @Nullable
    public QuantityType<Power> getLevel(int level) {
        int numNodes = getNumValueLevels();
        Point2D.Float p0 = new Point2D.Float(), p1 = new Point2D.Float();

        switch (numNodes) {
            case 0:
                return null;
            case 1:
                p0.setLocation(0f, 0f);
                p1.setLocation(100f, getPowerFloatValue(0));

                break;
            case 2:
                p0.setLocation(0f, getPowerFloatValue(0));
                p1.setLocation(100f, getPowerFloatValue(1));

                break;
            default: // more than 2
                p1.setLocation(0f, getPowerFloatValue(0));
                for (int i = 1; i < numNodes; i++) {
                    p0.setLocation(p1);
                    p1.setLocation(i * (100f / (numNodes - 1)), getPowerFloatValue(i));

                    if (level >= p0.getX() && level <= p1.getX()) { // found bounding points
                        break;
                    }
                }
                break;
        }

        // p0 and p1 are set at this point
        float floatLevel = (float) (p0.getY()
                + (level - p0.getX()) * (p1.getY() - p0.getY()) / (p1.getX() - p0.getX()));

        QuantityType<Power> qt = new QuantityType<Power>(floatLevel, Units.WATT);

        return qt;
    }

    /*
     * get power level for a specific state
     */
    @Nullable
    public QuantityType<Power> getLevel(String level) {
        String ucLevel = level.toUpperCase();

        QuantityType<Power> result = (powerStateLevels.get(ucLevel) instanceof QuantityType<Power> qt)
                ? qt.toUnit(Units.WATT)
                : null;

        // Try ON/OFF values for 100/0
        if (result == null) {
            int intLevel = "ON".equals(ucLevel) ? 100 : "OFF".equals(ucLevel) ? 0 : -1;

            if (intLevel != -1) {
                result = getLevel(intLevel);
            }
        }

        if (result == null) {
            return null;
        }

        return result;
    }

    private QuantityType<?> parseQuantityType(String s, Unit<?> defaultUnit) {
        QuantityType<?> qt = new QuantityType<>(s.trim());

        if (qt.getUnit() == Units.ONE) {
            // assume W unit
            return new QuantityType<>(qt.floatValue(), defaultUnit);
        }
        if (!qt.getUnit().isCompatible(Units.WATT)) {
            throw new IllegalArgumentException();
        }

        return qt;
    }

    private float getPowerFloatValue(int level) {
        if (powerValueLevels.get(level) instanceof QuantityType<Power> qt
                && qt.toUnit(Units.WATT) instanceof QuantityType<Power> qtW) {
            return qtW.floatValue();
        }

        return 0f;
    }
}
