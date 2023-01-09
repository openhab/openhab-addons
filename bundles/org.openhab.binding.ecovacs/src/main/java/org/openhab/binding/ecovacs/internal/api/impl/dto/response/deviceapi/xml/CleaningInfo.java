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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.SuctionPower;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.w3c.dom.Node;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class CleaningInfo {
    public static class CleanStateInfo {
        public final CleanMode mode;
        public final Optional<String> areaDefinition;

        CleanStateInfo(CleanMode mode) {
            this(mode, Optional.empty());
        }

        CleanStateInfo(CleanMode mode, Optional<String> areaDefinition) {
            this.mode = mode;
            this.areaDefinition = areaDefinition;
        }
    }

    public static CleanStateInfo parseCleanStateInfo(String xml, Gson gson) throws DataParsingException {
        String stateString = XPathUtils.getFirstXPathMatchOpt(xml, "//clean/@st").map(n -> n.getNodeValue()).orElse("");

        if ("h".equals(stateString)) {
            return new CleanStateInfo(CleanMode.STOP);
        } else if ("p".equals(stateString)) {
            return new CleanStateInfo(CleanMode.PAUSE);
        } else {
            String modeString = XPathUtils.getFirstXPathMatch(xml, "//clean/@type").getNodeValue();
            CleanMode parsedMode = gson.fromJson(modeString, CleanMode.class);
            if (parsedMode == CleanMode.SPOT_AREA) {
                Optional<Node> pointOpt = XPathUtils.getFirstXPathMatchOpt(xml, "//clean/@p");
                if (pointOpt.isPresent()) {
                    return new CleanStateInfo(CleanMode.CUSTOM_AREA, pointOpt.map(n -> n.getNodeValue()));
                }
                Optional<Node> midOpt = XPathUtils.getFirstXPathMatchOpt(xml, "//clean/@mid");
                return new CleanStateInfo(CleanMode.SPOT_AREA, midOpt.map(n -> n.getNodeValue()));
            }
            if (parsedMode != null) {
                return new CleanStateInfo(parsedMode);
            }
        }
        throw new DataParsingException("Unexpected clean state report: " + xml);
    }

    public static SuctionPower parseCleanSpeedInfo(String xml, Gson gson) throws DataParsingException {
        String levelString = XPathUtils.getFirstXPathMatch(xml, "//@speed").getNodeValue();
        SuctionPower level = gson.fromJson(levelString, SuctionPower.class);
        if (level == null) {
            throw new DataParsingException("Could not parse power level " + levelString);
        }
        return level;
    }
}
