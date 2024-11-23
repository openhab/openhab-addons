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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.util.Arrays;

/**
 * The XML Tags to extract the relevant parts from the API response.
 * The names map directly to the XML Tags of the API Response.
 *
 * @author Martin Koehler - Initial contribution
 */
public enum DwdXmlTag {

    UNKNOWN(""),
    SEVERITY("SEVERITY"),
    DESCRIPTION("DESCRIPTION"),
    EFFECTIVE("EFFECTIVE"),
    EXPIRES("EXPIRES"),
    ONSET("ONSET"),
    EVENT("EVENT"),
    STATUS("STATUS"),
    MSGTYPE("MSGTYPE"),
    HEADLINE("HEADLINE"),
    ALTITUDE("ALTITUDE"),
    CEILING("CEILING"),
    INSTRUCTION("INSTRUCTION"),
    URGENCY("URGENCY"),
    IDENTIFIER("IDENTIFIER"),
    WARNUNGEN_GEMEINDEN("Warnungen_Gemeinden");

    private String tag;

    private DwdXmlTag(String tag) {
        this.tag = tag;
    }

    String getTag() {
        return tag;
    }

    public static DwdXmlTag getDwdXmlTag(String tag) {
        return Arrays.asList(DwdXmlTag.values()).stream().filter(t -> tag.equals(t.getTag())).findFirst()
                .orElse(UNKNOWN);
    }
}
