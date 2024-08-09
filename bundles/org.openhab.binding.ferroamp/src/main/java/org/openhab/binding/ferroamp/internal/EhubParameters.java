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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EhubParameters} is responsible for all parameters regarded to EHUB
 *
 * @author Örjan Backsell - Initial contribution
 * @author Joel Backsell - Defined parameter records
 *
 */

@NonNullByDefault
public class EhubParameters {
    public String jsonPostEhub;

    public EhubParameters(String jsonPostEhub) {
        this.jsonPostEhub = jsonPostEhub;
    }

    public static List<String> getChannelParametersEhub() {
        final List<String> channelParametersEhub = Arrays.asList("wloadConsql1", "wloadConsql2", "wloadConsql3",
                "iloaddl1", "iloaddl2", "iloaddl3", "winvconsq_3p", "wextconsql1", "wextconsql2", "wextconsql3",
                "winvprodq_3p", "winvprodq_3p", "winvconsql1", "winvconsql2", "winvconsql3", "iextl1", "iextl2",
                "iextl3", "iloadql1", "iloadql2", "iloadql3", "wloadprodq_3p", "iacel1", "iacel2", "iacel3", "ploadl1",
                "ploadl2", "ploadl3", "plnvreactivel1", "plnvreactivel2", "plnvreactivel3", "ts", "ploadreactivel1",
                "ploadreactivel2", "ploadreactivel3", "state", "wloadprodql1", "wloadprodql2", "wloadprodql3", "ppv",
                "pinvl1", "pinvl2", "pinvl3", "iextql1", "iextql2", "iextql3", "pextl1", "pextl2", "pextl3",
                "wextprodql1", "wextprodql2", "wextprodql3", "wpv", "pextreactivel1", "pextreactivel2",
                "pextreactivel3", "udcpos", "udcneg", "sext", "iexdtl1", "iexdtl2", "iexdtl3", "wextconsq_3p", "ildl1",
                "ildl2", "ildl3", "gridfreq", "wloadconsq_3p", "ull1", "ull2", "ull3", "wextprodq_3p", "ilql1", "ilql2",
                "ilql3", "winvprodql1", "winvprodql2", "winvprodql3", "ill1", "ill2", "ill3", "wbatprod", "wpbatcons",
                "soc", "soh", "pbat", "ratedcap");
        return channelParametersEhub;
    }
}
