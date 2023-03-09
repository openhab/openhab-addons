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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.model.MoppingWaterAmount;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.w3c.dom.Node;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class WaterSystemInfo {
    /**
     * @return Whether water system is present
     */
    public static boolean parseWaterBoxInfo(String xml) throws DataParsingException {
        Node node = XPathUtils.getFirstXPathMatch(xml, "//@on");
        return Integer.valueOf(node.getNodeValue()) != 0;
    }

    public static MoppingWaterAmount parseWaterPermeabilityInfo(String xml) throws DataParsingException {
        Node node = XPathUtils.getFirstXPathMatch(xml, "//@v");
        try {
            return MoppingWaterAmount.fromApiValue(Integer.valueOf(node.getNodeValue()));
        } catch (NumberFormatException e) {
            throw new DataParsingException(e);
        }
    }
}
