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

/**
 *
 * @author Karel Goderis - Initial contribution
 */

public enum KNXDPTRule {

    ENERGY() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.ACTIVEENERGY) && ruleSet.contains(KNXDPTEvaluation.KWH)) {
                return KNXDPTEvaluation.KWH;
            }

            if (ruleSet.contains(KNXDPTEvaluation.ACTIVEENERGY) && ruleSet.contains(KNXDPTEvaluation.WH)) {
                return KNXDPTEvaluation.WH;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.WH)) {
                return KNXDPTEvaluation.WH;
            }

            return null;
        }
    },
    MOTION() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.ONEBIT) && ruleSet.contains(KNXDPTEvaluation.STARTOFMOTION)) {
                return KNXDPTEvaluation.STARTOFMOTION;
            }

            if (ruleSet.contains(KNXDPTEvaluation.ONEBIT) && ruleSet.contains(KNXDPTEvaluation.ENDOFMOTION)) {
                return KNXDPTEvaluation.ENDOFMOTION;
            }

            if (ruleSet.contains(KNXDPTEvaluation.ONEBIT) && ruleSet.contains(KNXDPTEvaluation.STARTOFPRESENCE)) {
                return KNXDPTEvaluation.STARTOFPRESENCE;
            }

            if (ruleSet.contains(KNXDPTEvaluation.ONEBIT) && ruleSet.contains(KNXDPTEvaluation.ENDOFPRESENCE)) {
                return KNXDPTEvaluation.ENDOFPRESENCE;
            }

            return null;
        }
    },
    TEMP_SENSOR_STATUS() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.TEMPERATURE) && ruleSet.contains(KNXDPTEvaluation.ONEBIT)) {
                return KNXDPTEvaluation.ONEBIT;
            }

            return null;
        }
    },
    CLIMATE() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.HUMIDITY)) {
                return KNXDPTEvaluation.HUMIDITY;
            }

            if (ruleSet.contains(KNXDPTEvaluation.PERCENTAGE) && ruleSet.contains(KNXDPTEvaluation.HUMIDITY)) {
                return KNXDPTEvaluation.PERCENTAGE;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.TEMPERATURE)) {
                return KNXDPTEvaluation.TEMPERATURE;
            }

            return null;
        }
    },
    POWER() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.POWER)) {
                return KNXDPTEvaluation.POWER;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.POWER)
                    && ruleSet.contains(KNXDPTEvaluation.MEASUREMENT)) {
                return KNXDPTEvaluation.POWER;
            }

            return null;
        }
    },
    MEASUREMENT() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.MEASUREMENT)
                    && !ruleSet.contains(KNXDPTEvaluation.POWER)) {
                return KNXDPTEvaluation.MEASUREMENT;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.CONSUMPTION)) {
                return KNXDPTEvaluation.CONSUMPTION;
            }

            if (ruleSet.contains(KNXDPTEvaluation.TIME) && ruleSet.contains(KNXDPTEvaluation.CONSUMPTION)) {
                return KNXDPTEvaluation.TIME;
            }

            return null;
        }
    },
    ALARM() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.ONEBIT) && ruleSet.contains(KNXDPTEvaluation.ANALOG)) {
                return KNXDPTEvaluation.ONEBIT;
            }

            return null;
        }
    },
    DATETIME() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.DATE) && ruleSet.contains(KNXDPTEvaluation.TIME)) {
                return KNXDPTEvaluation.DATE;
            }

            if (ruleSet.contains(KNXDPTEvaluation.THREEBYTE) && ruleSet.contains(KNXDPTEvaluation.TIME)) {
                return KNXDPTEvaluation.TIME;
            }

            if (ruleSet.contains(KNXDPTEvaluation.THREEBYTE) && ruleSet.contains(KNXDPTEvaluation.DATE)) {
                return KNXDPTEvaluation.DATE;
            }

            return null;
        }
    },
    GEO() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.AZIMUTH)) {
                return KNXDPTEvaluation.AZIMUTH;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.LATITUDE)) {
                return KNXDPTEvaluation.LATITUDE;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.LONGITUDE)) {
                return KNXDPTEvaluation.LONGITUDE;
            }

            if (ruleSet.contains(KNXDPTEvaluation.FOURBYTE) && ruleSet.contains(KNXDPTEvaluation.ELEVATION)) {
                return KNXDPTEvaluation.ELEVATION;
            }
            return null;
        }
    },
    OPERATINGMODE() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.MODE) && ruleSet.contains(KNXDPTEvaluation.OPERATINGMODE)) {
                return KNXDPTEvaluation.OPERATINGMODE;
            }

            if (ruleSet.contains(KNXDPTEvaluation.MODE) && ruleSet.contains(KNXDPTEvaluation.TEMPERATURE)) {
                return KNXDPTEvaluation.TEMPERATURE;
            }

            return null;
        }
    },
    CONSUMPTION() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.CONSUMPTION) && ruleSet.contains(KNXDPTEvaluation.CONSUMPTIONRESET)) {
                return KNXDPTEvaluation.CONSUMPTIONRESET;
            }

            return null;
        }
    },
    FAN() {
        @Override
        public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> ruleSet) {
            if (ruleSet.contains(KNXDPTEvaluation.FANSPEED) && ruleSet.contains(KNXDPTEvaluation.ONEBYTE)) {
                return KNXDPTEvaluation.FANSPEED;
            }

            return null;
        }
    };

    public KNXDPTEvaluation evaluate(Set<KNXDPTEvaluation> matches) {
        return null;
    }

    public static KNXDPTEvaluation evaluateAll(Set<KNXDPTEvaluation> evaluations) {

        HashSet<KNXDPTEvaluation> matches = new HashSet<KNXDPTEvaluation>();

        for (KNXDPTRule c : KNXDPTRule.values()) {
            KNXDPTEvaluation check = c.evaluate(evaluations);
            if (check != null) {
                matches.add(check);
            }
        }

        if (matches.size() == 0) {
            return null;
        } else if (matches.size() == 1) {
            return (KNXDPTEvaluation) matches.toArray()[0];
        } else {
            return null;
        }
    }

    public static Set<KNXDPTRule> getMatchingRules(Set<KNXDPTEvaluation> evaluations) {

        HashSet<KNXDPTRule> matches = new HashSet<KNXDPTRule>();

        for (KNXDPTRule c : KNXDPTRule.values()) {
            KNXDPTEvaluation check = c.evaluate(evaluations);
            if (check != null) {
                matches.add(c);
            }
        }

        return matches;
    }

}
