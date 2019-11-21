/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.kostalinverterpikonewgeneration.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Örjan Backsell - Initial contribution (New Generation)
 */
public class DxsEntriesCfgExt {

    public static String[] getDxsEntriesCfgExt(String dxsEntriesCfgFileExt) {
        List<String> dxsEntriesListExt = new ArrayList<>(23);
        dxsEntriesListExt.add("83886336");
        dxsEntriesListExt.add("83886592");
        dxsEntriesListExt.add("83886848");
        dxsEntriesListExt.add("83887106");
        dxsEntriesListExt.add("83887362");
        dxsEntriesListExt.add("83887618");
        dxsEntriesListExt.add("67110400");
        dxsEntriesListExt.add("67110656");
        dxsEntriesListExt.add("251659010");
        dxsEntriesListExt.add("251659266");
        dxsEntriesListExt.add("251659278");
        dxsEntriesListExt.add("251659279");
        dxsEntriesListExt.add("251659009");
        dxsEntriesListExt.add("251659265");
        dxsEntriesListExt.add("251658496");
        dxsEntriesListExt.add("33556238");
        dxsEntriesListExt.add("33556230");
        dxsEntriesListExt.add("33556228");
        dxsEntriesListExt.add("33556227");
        dxsEntriesListExt.add("150995968");
        dxsEntriesListExt.add("184549632");
        dxsEntriesListExt.add("251659280");
        dxsEntriesListExt.add("251659281");

        // Create an dxsEntriesExt array
        String[] dxsEntriesExt = new String[23];

        dxsEntriesListExt.toArray(dxsEntriesExt);

        return dxsEntriesExt;
    }
}
