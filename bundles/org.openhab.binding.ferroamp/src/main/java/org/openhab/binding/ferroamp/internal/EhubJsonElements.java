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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EhubParameters1} is responsible for all parameters regarded to EHUB
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
        jsonElementsEhub.add(new String("gridfreq"));
        jsonElementsEhub.add(new String("iace"));
        jsonElementsEhub.add(new String("ul"));
        jsonElementsEhub.add(new String("il"));
        jsonElementsEhub.add(new String("ild"));
        jsonElementsEhub.add(new String("ilq"));
        jsonElementsEhub.add(new String("iext"));
        jsonElementsEhub.add(new String("iextd"));
        jsonElementsEhub.add(new String("iextq"));
        jsonElementsEhub.add(new String("iloadd"));
        jsonElementsEhub.add(new String("iloadq"));
        jsonElementsEhub.add(new String("sext"));
        jsonElementsEhub.add(new String("pext"));
        jsonElementsEhub.add(new String("pextreactive"));
        jsonElementsEhub.add(new String("pinv"));
        jsonElementsEhub.add(new String("pinvreactive"));
        jsonElementsEhub.add(new String("pload"));
        jsonElementsEhub.add(new String("ploadreactive"));
        jsonElementsEhub.add(new String("ppv"));
        jsonElementsEhub.add(new String("udc"));
        jsonElementsEhub.add(new String("wextprodq"));
        jsonElementsEhub.add(new String("wextconsq"));
        jsonElementsEhub.add(new String("winvprodq"));
        jsonElementsEhub.add(new String("winvconsq"));
        jsonElementsEhub.add(new String("wloadprodq"));
        jsonElementsEhub.add(new String("wloadconsq"));
        jsonElementsEhub.add(new String("wextprodq_3p"));
        jsonElementsEhub.add(new String("wextconsq_3p"));
        jsonElementsEhub.add(new String("winvprodq_3p"));
        jsonElementsEhub.add(new String("winvconsq_3p"));
        jsonElementsEhub.add(new String("wloadprodq_3p"));
        jsonElementsEhub.add(new String("wloadconsq_3p"));
        jsonElementsEhub.add(new String("wpv"));
        jsonElementsEhub.add(new String("state"));
        jsonElementsEhub.add(new String("ts"));
        jsonElementsEhub.add(new String("wbatprod"));
        jsonElementsEhub.add(new String("wpbatcons"));
        jsonElementsEhub.add(new String("soc"));
        jsonElementsEhub.add(new String("soh"));
        jsonElementsEhub.add(new String("pbat"));
        jsonElementsEhub.add(new String("ratedcap"));
        return jsonElementsEhub;
    }
}
