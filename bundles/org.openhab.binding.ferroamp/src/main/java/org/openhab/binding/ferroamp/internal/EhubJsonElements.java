/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EhubJsonElements} is responsible for all parameters related to EHUB
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class EhubJsonElements {
    public String ehubJsonElements;

    public EhubJsonElements(String ehubJsonElements) {
        this.ehubJsonElements = ehubJsonElements;
    }

    public static List<String> getJsonElementsEhub() {
        final List<String> jsonElementsEhub = new ArrayList<>();
        jsonElementsEhub.add("gridfreq");
        jsonElementsEhub.add("iace");
        jsonElementsEhub.add("ul");
        jsonElementsEhub.add("il");
        jsonElementsEhub.add("ild");
        jsonElementsEhub.add("ilq");
        jsonElementsEhub.add("iext");
        jsonElementsEhub.add("iextd");
        jsonElementsEhub.add("iextq");
        jsonElementsEhub.add("iloadd");
        jsonElementsEhub.add("iloadq");
        jsonElementsEhub.add("sext");
        jsonElementsEhub.add("pext");
        jsonElementsEhub.add("pextreactive");
        jsonElementsEhub.add("pinv");
        jsonElementsEhub.add("pinvreactive");
        jsonElementsEhub.add("pload");
        jsonElementsEhub.add("ploadreactive");
        jsonElementsEhub.add("ppv");
        jsonElementsEhub.add("udc");
        jsonElementsEhub.add("wextprodq");
        jsonElementsEhub.add("wextconsq");
        jsonElementsEhub.add("winvprodq");
        jsonElementsEhub.add("winvconsq");
        jsonElementsEhub.add("wloadprodq");
        jsonElementsEhub.add("wloadconsq");
        jsonElementsEhub.add("wextprodq_3p");
        jsonElementsEhub.add("wextconsq_3p");
        jsonElementsEhub.add("winvprodq_3p");
        jsonElementsEhub.add("winvconsq_3p");
        jsonElementsEhub.add("wloadprodq_3p");
        jsonElementsEhub.add("wloadconsq_3p");
        jsonElementsEhub.add("wpv");
        jsonElementsEhub.add("state");
        jsonElementsEhub.add("ts");
        jsonElementsEhub.add("wbatprod");
        jsonElementsEhub.add("wpbatcons");
        jsonElementsEhub.add("soc");
        jsonElementsEhub.add("soh");
        jsonElementsEhub.add("pbat");
        jsonElementsEhub.add("ratedcap");
        return jsonElementsEhub;
    }
}
