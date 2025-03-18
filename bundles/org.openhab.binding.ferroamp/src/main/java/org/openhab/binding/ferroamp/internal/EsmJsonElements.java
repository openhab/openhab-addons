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
 * The {@link EhubParameters1} is responsible for all parameters regarded to EHUB
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class EsmJsonElements {
    public String esmJsonElements;

    public EsmJsonElements(String esmJsonElements) {
        this.esmJsonElements = esmJsonElements;
    }

    public static List<String> getJsonElementsEsm() {
        final List<String> jsonElementsEsm = new ArrayList<>();
        jsonElementsEsm.add(new String("id"));
        jsonElementsEsm.add(new String("soh"));
        jsonElementsEsm.add(new String("soc"));
        jsonElementsEsm.add(new String("ratedcapacity"));
        jsonElementsEsm.add(new String("ratedpower"));
        jsonElementsEsm.add(new String("status"));
        jsonElementsEsm.add(new String("ts"));
        return jsonElementsEsm;
    }
}
