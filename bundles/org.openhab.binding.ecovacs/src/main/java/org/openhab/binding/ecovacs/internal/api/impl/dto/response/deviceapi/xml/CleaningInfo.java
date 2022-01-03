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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.SuctionPower;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class CleaningInfo {
    public static CleanMode parseCleanStateInfo(String xml, Gson gson) throws Exception {
        String stateString = XPathUtils.getFirstXPathMatchOpt(xml, "//clean/@st").map(n -> n.getNodeValue()).orElse("");
        final CleanMode mode;

        if ("h".equals(stateString)) {
            mode = CleanMode.STOP;
        } else if ("p".equals(stateString)) {
            mode = CleanMode.PAUSE;
        } else {
            String modeString = XPathUtils.getFirstXPathMatch(xml, "//clean/@type").getNodeValue();
            mode = gson.fromJson(modeString, CleanMode.class);
        }
        if (mode != null) {
            return mode;
        }
        throw new IllegalArgumentException("Unexpected clean state report: " + xml);
    }

    public static SuctionPower parseCleanSpeedInfo(String xml, Gson gson) throws Exception {
        String levelString = XPathUtils.getFirstXPathMatch(xml, "//@speed").getNodeValue();
        SuctionPower level = gson.fromJson(levelString, SuctionPower.class);
        if (level == null) {
            throw new IllegalArgumentException("Could not parse power level " + levelString);
        }
        return level;
    }
}
