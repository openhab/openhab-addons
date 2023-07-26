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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml;

import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.model.ChargeMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.w3c.dom.Node;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class DeviceInfo {
    private static final Set<String> ERROR_ATTR_NAMES = Set.of("code", "error", "errno", "errs");

    public static int parseBatteryInfo(String xml) throws DataParsingException {
        Node batteryAttr = XPathUtils.getFirstXPathMatch(xml, "//battery/@power");
        return Integer.valueOf(batteryAttr.getNodeValue());
    }

    public static ChargeMode parseChargeInfo(String xml, Gson gson) throws DataParsingException {
        String modeString = XPathUtils.getFirstXPathMatch(xml, "//charge/@type").getNodeValue();
        ChargeMode mode = gson.fromJson(modeString, ChargeMode.class);
        if (mode == null) {
            throw new IllegalArgumentException("Could not parse charge mode " + modeString);
        }
        return mode;
    }

    public static Optional<Integer> parseErrorInfo(String xml) throws DataParsingException {
        for (String attr : ERROR_ATTR_NAMES) {
            Optional<Node> node = XPathUtils.getFirstXPathMatchOpt(xml, "//@" + attr);
            if (node.isPresent()) {
                try {
                    String value = node.get().getNodeValue();
                    return value.isEmpty() ? Optional.empty() : Optional.of(Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    throw new DataParsingException(e);
                }
            }
        }
        return Optional.empty();
    }

    public static int parseComponentLifespanInfo(String xml) throws DataParsingException {
        Optional<Integer> value = nodeValueToInt(xml, "value");
        Optional<Integer> total = nodeValueToInt(xml, "total");
        Optional<Integer> left = nodeValueToInt(xml, "left");
        if (value.isPresent() && total.isPresent()) {
            return (int) Math.round(100.0 * value.get() / total.get());
        } else if (value.isPresent()) {
            return (int) Math.round(0.01 * value.get());
        } else if (left.isPresent() && total.isPresent()) {
            return (int) Math.round(100.0 * left.get() / total.get());
        } else if (left.isPresent()) {
            return (int) Math.round((double) left.get() / 60.0);
        }
        return 0;
    }

    public static boolean parseEnabledStateInfo(String xml) throws DataParsingException {
        String value = XPathUtils.getFirstXPathMatch(xml, "//@on").getNodeValue();
        try {
            return Integer.valueOf(value) != 0;
        } catch (NumberFormatException e) {
            throw new DataParsingException(e);
        }
    }

    private static Optional<Integer> nodeValueToInt(String xml, String attrName) throws DataParsingException {
        try {
            return XPathUtils.getFirstXPathMatchOpt(xml, "//ctl/@" + attrName)
                    .map(n -> Integer.valueOf(n.getNodeValue()));
        } catch (NumberFormatException e) {
            throw new DataParsingException(e);
        }
    }
}
