/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.parser;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.LoggerFactory;

/**
 *
 * @author Karel Goderis - Initial contribution
 */

public enum KNXDPTException {

    MDT_CURRENT("7.012") {
        @Override
        public boolean verify(String dpt, String deviceManufacturer, String deviceName, String input) {
            if (deviceManufacturer != null && dpt != null && input != null) {
                if (deviceManufacturer.contains("MDT") && dpt.equals("14.019") && input.equals("2 Bytes")) {
                    return true;
                }
            }

            return false;
        }
    },
    ARCUS_TEMP("9.001") {
        @Override
        public boolean verify(Set<String> dpts, String deviceManufacturer, String deviceName, String input) {
            if (deviceManufacturer != null && dpts != null && deviceName != null) {
                if (deviceManufacturer.contains("Arcus-eds") && deviceName.contains("S8-WAQ")) {
                    if (dpts.contains("7.001") && dpts.contains("9.001")) {
                        return true;
                    }
                }
            }

            return false;
        }
    };

    private String dpt;

    private KNXDPTException(String dpt) {
        this.dpt = dpt;
    }

    public String getDPT() {
        return dpt;
    }

    public boolean verify(String dpt, String deviceManufacturer, String deviceName, String input) {
        return false;
    }

    public boolean verify(Set<String> dpts, String deviceManufacturer, String deviceName, String input) {
        return false;
    }

    public static Set<KNXDPTException> getMatchingExceptions(String dpt, String deviceManufacturer, String deviceName,
            String input) {

        Set<KNXDPTException> matches = new HashSet<KNXDPTException>();

        for (KNXDPTException c : KNXDPTException.values()) {
            if (c.verify(dpt, deviceManufacturer, deviceName, input)) {
                matches.add(c);
            }
        }

        return matches;
    }

    public static Set<KNXDPTException> getMatchingExceptions(Set<String> dpts, String deviceManufacturer,
            String deviceName, String input) {

        Set<KNXDPTException> matches = new HashSet<KNXDPTException>();

        for (KNXDPTException c : KNXDPTException.values()) {
            if (c.verify(dpts, deviceManufacturer, deviceName, input)) {
                matches.add(c);
            }
        }

        return matches;
    }

    public static String transform(String dpt, String deviceManufacturer, String deviceName, String input) {

        Set<KNXDPTException> matches = getMatchingExceptions(dpt, deviceManufacturer, deviceName, input);

        if (matches.size() == 0) {
            return dpt;
        } else if (matches.size() == 1) {
            return ((KNXDPTException) matches.toArray()[0]).getDPT();
        } else {
            LoggerFactory.getLogger(KNXDPTRule.class).warn(
                    "More than one exception rule exists for dpt {}, manufacturer {}, device {}, constraint {} : {}",
                    dpt, deviceManufacturer, deviceName, input, matches);
        }

        return dpt;
    }

    public static String transform(Set<String> dpts, String deviceManufacturer, String deviceName, String input) {

        Set<KNXDPTException> matches = getMatchingExceptions(dpts, deviceManufacturer, deviceName, input);

        if (matches.size() == 0) {
            return null;
        } else if (matches.size() == 1) {
            return ((KNXDPTException) matches.toArray()[0]).getDPT();
        } else {
            LoggerFactory.getLogger(KNXDPTRule.class).warn(
                    "More than one exception rule exists for dpts {}, manufacturer {}, device {}, constraint {} : {}",
                    dpts, deviceManufacturer, deviceName, input, matches);
        }

        return null;
    }

}
