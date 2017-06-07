/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.parser;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Karel Goderis - Initial contribution
 */

public enum KNXDPTEvaluation {

    ONEBIT("1.001") {
        @Override
        public boolean evaluate(String expression) {
            return expression.equals("1 Bit");
        }
    },
    STARTOFPRESENCE("1.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "START OF PRESENCE");
        }
    },
    ENDOFPRESENCE("1.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "END OF PRESENCE");
        }
    },
    STARTOFMOTION("1.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "START OF MOTION");
        }
    },
    ENDOFMOTION("1.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "END OF MOTION");
        }
    },
    PERCENTAGE("5.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "%");
        }
    },
    ONEBYTE("5.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "1 BYTE");
        }
    },
    OPERATINGMODE("5.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "OPERATING MODE");
        }
    },
    MODE("5.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "MODE");
        }
    },
    SCENE("5.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "SCENE");
        }
    },
    FANSPEED("5.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "FAN SPEED");
        }
    },
    OPERATINGHOURS("7.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "OPERATING HOURS");
        }
    },
    ANALOG("9.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "ANALOG");
        }
    },
    TEMPERATURE("9.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "TEMPERATURE")
                    || StringUtils.contains(StringUtils.upperCase(expression), "CELSIUS");
        }
    },
    CONSUMPTIONRESET("9.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "CONSUMPTION VALUE RESET");
        }
    },
    CURVELEVEL("9.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "CURVE LEVEL");
        }
    },
    LUX("9.004") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "LUX")
                    || StringUtils.contains(StringUtils.upperCase(expression), "BRIGHTNESS");
        }
    },
    WIND("9.005") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "WIND SPEED");
        }
    },
    HUMIDITY("9.007") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "HUMIDITY");
        }
    },
    TIME("10.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "TIME");
        }
    },
    DATE("11.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "DATE");
        }
    },
    THREEBYTE("11.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "3 BYTES");
        }
    },
    BURNER("12.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "BURNER STARTS")
                    || StringUtils.contains(StringUtils.upperCase(expression), "BURNER HOURS");
        }
    },
    ACTIVEENERGY("13.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "ACTIVE ENERGY");
        }
    },
    WH("13.010") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(expression, "Wh");
        }
    },
    KWH("13.013") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(expression, "kWh");
        }
    },
    FOURBYTE("14.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "4 BYTES");
        }
    },
    MEASUREMENT("14.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "MEASUREMENT");
        }
    },
    CONSUMPTION("14.001") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "CONSUMPTION");
        }
    },
    AZIMUTH("14.007") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "AZIMUTH");
        }
    },
    ELEVATION("14.007") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "ELEVATION");
        }
    },
    LATITUDE("14.007") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "LATITUDE");
        }
    },
    LONGITUDE("14.007") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "LONGITUDE");
        }
    },
    POWER("14.056") {
        @Override
        public boolean evaluate(String expression) {
            return StringUtils.contains(StringUtils.upperCase(expression), "POWER");
        }
    };

    private String dpt;

    private KNXDPTEvaluation(String dpt) {
        this.dpt = dpt;
    }

    public boolean evaluate(String expression) {
        return false;
    }

    public String getDPT() {
        return dpt;
    }

    public static Set<KNXDPTEvaluation> getMatchingEvaluations(String expression) {

        HashSet<KNXDPTEvaluation> matchingRules = new HashSet<KNXDPTEvaluation>();

        for (KNXDPTEvaluation c : KNXDPTEvaluation.values()) {
            if (c.evaluate(expression)) {
                matchingRules.add(c);
            }
        }

        return matchingRules;
    }

}
