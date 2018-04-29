/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.parser;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.knx.internal.ets.parser.knxproj13.GroupAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.KNXException;

/**
 * KNX Parser Advisor(ies) are used to help the parser determine the DPT of a given knxproj-driven Group Address. The
 * fact is that often KNX vendors are not following the standards for the XML-based device definitions in a coherent
 * manner, often including no or wrong information regarding the DPT of a given Communication Object.
 *
 * {link KNXDTPAdvisor} will feed back a suggested {link KNXDTPContext} based on a set of information that is provided.
 * It will traverse a set of Advisories and return the set of Advisories that do match the criteria.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public class KNXDPTAdvisor {

    private final Logger logger = LoggerFactory.getLogger(KNXDPTAdvisor.class);

    private final Set<AdvisoryFunction> advisorySet;

    public KNXDPTAdvisor() {
        advisorySet = new HashSet<>();

        // String Specific Advisories

        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (expression != null && expression.equals("1 Bit")) {
                return new KNXDPTContext("1.001", "1 Bit");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "START OF PRESENCE")) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "END OF PRESENCE")) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });

        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "START OF MOTION")) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "END OF MOTION")) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "%")) {
                return new KNXDPTContext("5.001", "PERCENT");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (expression != null && expression.equals("1 Byte")) {
                return new KNXDPTContext("5.010", "1 BYTE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "OPERATING MODE")) {
                return new KNXDPTContext("5.010", "OPERATING MODE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "MODE")) {
                return new KNXDPTContext("5.010", "MODE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "SCENE")) {
                return new KNXDPTContext("5.010", "SCENE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "FAN SPEED")) {
                return new KNXDPTContext("5.010", "FAN SPEED");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "OPERATING HOURS")) {
                return new KNXDPTContext("7.001", "OPERATING HOURS");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "ANALOG")) {
                return new KNXDPTContext("9.001", "ANALOG");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "TEMPERATURE")
                    || StringUtils.contains(StringUtils.upperCase(expression), "CELSIUS")) {
                return new KNXDPTContext("9.001", "TEMPERATURE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "CONSUMPTION VALUE RESET")) {
                return new KNXDPTContext("9.001", "CONSUMPTION RESET");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "CURVE LEVEL")) {
                return new KNXDPTContext("9.001", "CURVE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "LUX")
                    || StringUtils.contains(StringUtils.upperCase(expression), "BRIGHTNESS")) {
                return new KNXDPTContext("9.004", "LUX");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "WIND SPEED")) {
                return new KNXDPTContext("9.005", "WIND");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "HUMIDITY")) {
                return new KNXDPTContext("9.007", "HUMIDITY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "TIME")) {
                return new KNXDPTContext("10.001", "TIME");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "DATE")) {
                return new KNXDPTContext("11.001", "DATE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (expression != null && expression.equals("3 Bytes")) {
                return new KNXDPTContext("11.001", "3 BYTES");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (expression != null && expression.equals("4 Bytes")) {
                return new KNXDPTContext("14.001", "4 BYTES");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "BURNER STARTS")
                    || StringUtils.contains(StringUtils.upperCase(expression), "BURNER HOURS")) {
                return new KNXDPTContext("12.001", "BURNER");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "ACTIVE ENERGY")) {
                return new KNXDPTContext("13.010", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(expression, "Wh")) {
                return new KNXDPTContext("13.010", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(expression, "kWh")) {
                return new KNXDPTContext("13.013", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "MEASUREMENT")) {
                return new KNXDPTContext("14.001", "MEASUREMENT");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "CONSUMPTION")) {
                return new KNXDPTContext("14.001", "CONSUMPTION");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "AZIMUTH")) {
                return new KNXDPTContext("14.007", "GEO");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "ELEVATION")) {
                return new KNXDPTContext("14.007", "GEO");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "LATITUDE")) {
                return new KNXDPTContext("14.007", "GEO");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "LONGITUDE")) {
                return new KNXDPTContext("14.007", "GEO");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (StringUtils.contains(StringUtils.upperCase(expression), "POWER")) {
                return new KNXDPTContext("14.056", "ENERGY");
            }
            return null;
        });

        // DPT {Tuple} Specific Advisories

        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("13.010", "ENERGY"))
                    && dpts.contains(new KNXDPTContext("13.013", "ENERGY"))) {
                return new KNXDPTContext("13.013", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "4 BYTES"))
                    && dpts.contains(new KNXDPTContext("13.010", "ENERGY"))) {
                return new KNXDPTContext("13.010", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("1.001", "1 Bit"))
                    && dpts.contains(new KNXDPTContext("1.001", "PRESENCE"))) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("1.001", "PRESENCE"))
                    && dpts.contains(new KNXDPTContext("9.001", "TEMPERATURE"))) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("1.001", "PRESENCE"))
                    && dpts.contains(new KNXDPTContext("5.010", "SCENE"))) {
                return new KNXDPTContext("1.001", "PRESENCE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("1.001", "1 Bit"))
                    && dpts.contains(new KNXDPTContext("9.001", "TEMPERATURE"))) {
                return new KNXDPTContext("1.001", "1 Bit");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("5.001", "PERCENT"))
                    && dpts.contains(new KNXDPTContext("9.007", "HUMIDITY"))) {
                return new KNXDPTContext("5.001", "PERCENT");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "4 BYTES"))
                    && dpts.contains(new KNXDPTContext("14.056", "ENERGY"))) {
                return new KNXDPTContext("14.056", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "4 BYTES"))
                    && dpts.contains(new KNXDPTContext("14.056", "ENERGY"))
                    && dpts.contains(new KNXDPTContext("14.001", "MEASUREMENT"))) {
                return new KNXDPTContext("14.056", "ENERGY");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "4 BYTES"))
                    && !dpts.contains(new KNXDPTContext("14.056", "ENERGY"))
                    && dpts.contains(new KNXDPTContext("14.001", "MEASUREMENT"))) {
                return new KNXDPTContext("14.001", "MEASUREMENT");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "4 BYTES"))
                    && dpts.contains(new KNXDPTContext("14.001", "CONSUMPTION"))) {
                return new KNXDPTContext("14.001", "CONSUMPTION");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("10.001", "TIME"))
                    && dpts.contains(new KNXDPTContext("14.001", "CONSUMPTION"))) {
                return new KNXDPTContext("10.001", "TIME");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("1.001", "1 Bit"))
                    && dpts.contains(new KNXDPTContext("9.001", "ANALOG"))) {
                return new KNXDPTContext("1.001", "1 Bit");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("10.001", "TIME"))
                    && dpts.contains(new KNXDPTContext("11.001", "DATE"))) {
                return new KNXDPTContext("11.001", "DATE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("10.001", "TIME"))
                    && dpts.contains(new KNXDPTContext("11.001", "3 BYTES"))) {
                return new KNXDPTContext("10.001", "TIME");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("11.001", "DATE"))
                    && dpts.contains(new KNXDPTContext("11.001", "3 BYTES"))) {
                return new KNXDPTContext("11.001", "DATE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "4 BYTES"))
                    && dpts.contains(new KNXDPTContext("14.007", "GEO"))) {
                return new KNXDPTContext("14.007", "GEO");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("5.010", "OPERATING MODE"))
                    && dpts.contains(new KNXDPTContext("5.010", "MODE"))) {
                return new KNXDPTContext("5.010", "OPERATING MODE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("5.010", "MODE"))
                    && dpts.contains(new KNXDPTContext("9.001", "TEMPERATURE"))) {
                return new KNXDPTContext("9.001", "TEMPERATURE");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("14.001", "CONSUMPTION"))
                    && dpts.contains(new KNXDPTContext("9.001", "CONSUMPTION RESET"))) {
                return new KNXDPTContext("9.001", "CONSUMPTION RESET");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("5.010", "1 BYTE"))
                    && dpts.contains(new KNXDPTContext("5.010", "FAN SPEED"))) {
                return new KNXDPTContext("5.010", "FAN SPEED");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("9.001", "ANALOG"))
                    && dpts.contains(new KNXDPTContext("7.001", "SEED"))) {
                return new KNXDPTContext("9.001", "ANALOG");
            }
            return null;
        });

        // Device Specific Advisories

        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (dpts != null && dpts.contains(new KNXDPTContext("9.001", "ARCUS"))
                    && dpts.contains(new KNXDPTContext("7.001", "SEED"))) {
                return new KNXDPTContext("9.001", "ARCUS");
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("MDT") && expression.equals("2 Bytes")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("14.019")).findFirst().isPresent()) {
                        return new KNXDPTContext("7.012", "MDT");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("MDT")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("7.012")).findFirst().isPresent() && (dpts
                            .stream().filter(dpt -> dpt.getDpt().equals("14.019")).findFirst().isPresent()
                            || dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent())) {
                        return new KNXDPTContext("7.012", "MDT");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("MDT")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("1.008")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("1.009")).findFirst().isPresent()) {
                        return new KNXDPTContext("1.008", "MDT");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("MDT")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("5.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("5.010")).findFirst().isPresent()) {
                        return new KNXDPTContext("5.001", "MDT");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("MDT") && device.contains("Heating actuator")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("1.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("5.010")).findFirst().isPresent()) {
                        return new KNXDPTContext("1.001", "MDT");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("Viessmann")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("10.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("12.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent()) {
                        return new KNXDPTContext("12.001", "VIESSMANN");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("Viessmann")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("9.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent()) {
                        return new KNXDPTContext("9.001", "VIESSMANN");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("Viessmann")
                        && (expression.contains("Warmwassertemperatur oben") || expression.contains("Vorlauftemperatur")
                                || expression.contains("Niveau der Heizkennlinie"))) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("9.001")).findFirst().isPresent()
                            && dpts.contains(new KNXDPTContext("5.010", "OPERATING MODE"))) {
                        return new KNXDPTContext("5.010", "VIESSMANN");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("Viessmann")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("9.001")).findFirst().isPresent()
                            && dpts.contains(new KNXDPTContext("1.001", "1 Bit"))) {
                        return new KNXDPTContext("1.001", "VIESSMANN");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("Viessmann") && expression.contains("Aktuelle Betriebsart")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("9.001")).findFirst().isPresent()
                            && dpts.contains(new KNXDPTContext("5.010", "OPERATING MODE"))) {
                        return new KNXDPTContext("9.001", "VIESSMANN");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && expression != null) {
                if (manufacturer.contains("Viessmann")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("10.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("12.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent()) {
                        return new KNXDPTContext("12.001", "VIESSMANN");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("Arcus-eds") && device.contains("S8-WAQ")) {
                    if ((dpts.stream().filter(dpt -> dpt.getDpt().equals("7.001")).findFirst().isPresent()
                            || dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent())
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("9.001")).findFirst().isPresent()) {
                        return new KNXDPTContext("9.001", "ARCUS");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("Arcus-eds") && device.contains("S8-WAQ")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("9.020")).findFirst().isPresent()) {
                        return new KNXDPTContext("9.020", "ARCUS");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("Elsner Elektronik GmbH") && device.contains("KNX TH-UP basic")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("14.005")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("9.007")).findFirst().isPresent()) {
                        return new KNXDPTContext("14.005", "HUMIDITY");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("Elsner Elektronik GmbH") && device.contains("KNX TH-UP basic")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("14.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("9.007")).findFirst().isPresent()) {
                        return new KNXDPTContext("14.005", "HUMIDITY");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("Elsner Elektronik GmbH") && device.contains("KNX TH-UP basic")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("14.007")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("9.007")).findFirst().isPresent()) {
                        return new KNXDPTContext("14.007", "HUMIDITY");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("Elsner Elektronik GmbH") && device.contains("KNX TH-UP basic")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("14.017")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("9.007")).findFirst().isPresent()) {
                        return new KNXDPTContext("14.017", "HUMIDITY");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            if (manufacturer != null && dpts != null && device != null) {
                if (manufacturer.contains("GIRA") && device.contains("Analogue")) {
                    if (dpts.stream().filter(dpt -> dpt.getDpt().equals("9.001")).findFirst().isPresent()
                            && dpts.stream().filter(dpt -> dpt.getDpt().equals("7.001")).findFirst().isPresent()) {
                        return new KNXDPTContext("9.001", "GIRA");
                    }
                }
            }
            return null;
        });
        advisorySet.add((AdvisoryFunction) (dpts, GA, manufacturer, device, expression) -> {
            // return the ost frequent DPT, and if not a single DPT is most frequent, then return the DPT wit the
            // highest subnumber
            if (dpts != null && dpts.size() > 1) {

                if (!(dpts.stream().filter(k -> !"MOSTFREQUENT".equals(k.getContext())).map(k -> k.getDpt()).sorted()
                        .collect(Collectors.groupingBy(w -> w, Collectors.counting())).values().stream().distinct()
                        .count() == 1)) {
                    String mostFrequent = dpts.stream().filter(k -> !"MOSTFREQUENT".equals(k.getContext()))
                            .map(k -> k.getDpt()).sorted().collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                            .entrySet().stream().max(Comparator.comparing(Entry::getValue)).get().getKey();
                    return new KNXDPTContext(mostFrequent, "MOSTFREQUENT");
                } else {
                    String adviseContext = "";
                    int mainNumber = -1;
                    boolean sameMainNumber = true;

                    for (KNXDPTContext context : dpts) {
                        if (mainNumber == -1) {
                            mainNumber = getMainNumber(context.getDpt());
                        } else {
                            if (mainNumber != getMainNumber(context.getDpt())) {
                                sameMainNumber = false;
                            }
                        }
                    }

                    if (sameMainNumber && mainNumber != -1) {
                        int subNumber = -1;
                        for (KNXDPTContext context : dpts) {
                            if (getSubNumber(context.getDpt()) > subNumber) {
                                subNumber = getSubNumber(context.getDpt());
                                adviseContext = context.getContext();
                            }
                        }
                        if (subNumber != -1) {
                            return new KNXDPTContext(
                                    String.valueOf(mainNumber) + "." + String.format("%03d", subNumber), adviseContext);
                        }
                    }
                }
            }
            return null;
        });
    }

    @FunctionalInterface
    private interface AdvisoryFunction {
        KNXDPTContext apply(Set<KNXDPTContext> dpts, GroupAddress GA, String manufacturer, String device,
                String expression) throws KNXException;
    }

    /**
     * Provide a set of KNXDPTContext advise
     *
     * @param dpts a set of KNXDPTContexts, also called "seeds"
     * @param GA a Group Address
     * @param manufacturer the name of a KNX device manufacturer
     * @param device the name or type of a KNX device
     * @param expression an expression to serve as input for the evaluation
     * @param keepSeeds a boolean to indicate whether the "seeds" have to be added back to the resulting set
     * @return
     * @throws KNXException
     */
    public Set<KNXDPTContext> adviseOn(Set<KNXDPTContext> dpts, GroupAddress GA, String manufacturer, String device,
            String expression, boolean keepSeeds) throws KNXException {
        Set<KNXDPTContext> advise = new HashSet<KNXDPTContext>();

        logger.trace("Seeking advice for '{}'/'{}'/'{}'/'{}'", dpts, manufacturer, device, expression);

        for (AdvisoryFunction advisory : advisorySet) {
            KNXDPTContext partialAdvice = advisory.apply(dpts, GA, manufacturer, device, expression);
            if (partialAdvice != null) {
                logger.trace("Positive advise for '{}'/'{}'/'{}'/'{}' : '{}'", dpts, manufacturer, device, expression,
                        partialAdvice);
                advise.add(partialAdvice);
            }
        }

        if (keepSeeds) {
            advise.addAll(dpts);
        }

        logger.trace("Advise is '{}'", advise);
        return advise;
    }

    /**
     * Provide a set of KNXDPTContext advise for a given expression
     *
     * @param expression an expression to serves as input for the evaluation
     * @return
     * @throws KNXException
     */
    public Set<KNXDPTContext> adviseOn(String expression) throws KNXException {
        return adviseOn(null, null, null, null, expression, false);
    }

    /**
     * Retrieves sub number from a DTP ID such as "14.001"
     *
     * @param dptID String with DPT ID
     * @return sub number or -1
     */
    private int getSubNumber(String dptID) {
        int result = -1;
        if (dptID == null) {
            throw new IllegalArgumentException("Parameter dptID cannot be null");
        }

        int dptSepratorPosition = dptID.indexOf('.');
        if (dptSepratorPosition > 0) {
            try {
                result = Integer.parseInt(dptID.substring(dptSepratorPosition + 1, dptID.length()));
            } catch (NumberFormatException nfe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (NumberFormatException): {}",
                        dptID);
            } catch (IndexOutOfBoundsException ioobe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (IndexOutOfBoundsException): {}",
                        dptID);
            }
        }
        return result;
    }

    /**
     * Retrieves main number from a DTP ID such as "14.001"
     *
     * @param dptID String with DPT ID
     * @return main number or -1
     */
    private int getMainNumber(String dptID) {
        int result = -1;
        if (dptID == null) {
            throw new IllegalArgumentException("Parameter dptID cannot be null");
        }

        int dptSepratorPosition = dptID.indexOf('.');
        if (dptSepratorPosition > 0) {
            try {
                result = Integer.parseInt(dptID.substring(0, dptSepratorPosition));
            } catch (NumberFormatException nfe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (NumberFormatException): {}",
                        dptID);
            } catch (IndexOutOfBoundsException ioobe) {
                logger.error("toType couldn't identify main and/or sub number in dptID (IndexOutOfBoundsException): {}",
                        dptID);
            }
        }
        return result;
    }
}
