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
        final List<String> channelParametersEhub = new ArrayList<>();
        channelParametersEhub.add(new String("wloadconsqL1"));
        channelParametersEhub.add(new String("wloadconsqL2"));
        channelParametersEhub.add(new String("wloadconsqL3"));
        channelParametersEhub.add(new String("iloaddl1"));
        channelParametersEhub.add(new String("iloaddl2"));
        channelParametersEhub.add(new String("iloaddl3"));
        channelParametersEhub.add(new String("winvconsq_3p"));
        channelParametersEhub.add(new String("wextconsql1"));
        channelParametersEhub.add(new String("wextconsql2"));
        channelParametersEhub.add(new String("wextconsql3"));
        channelParametersEhub.add(new String("winvprodq_3p"));
        channelParametersEhub.add(new String("winvconsql1"));
        channelParametersEhub.add(new String("winvconsql2"));
        channelParametersEhub.add(new String("winvconsql3"));
        channelParametersEhub.add(new String("iextl1"));
        channelParametersEhub.add(new String("iextl2"));
        channelParametersEhub.add(new String("iextl3"));
        channelParametersEhub.add(new String("iloadql1"));
        channelParametersEhub.add(new String("iloadql2"));
        channelParametersEhub.add(new String("iloadql3"));
        channelParametersEhub.add(new String("wloadprodq_3p"));
        channelParametersEhub.add(new String("iacel1"));
        channelParametersEhub.add(new String("iacel2"));
        channelParametersEhub.add(new String("iacel3"));
        channelParametersEhub.add(new String("ploadl1"));
        channelParametersEhub.add(new String("ploadl2"));
        channelParametersEhub.add(new String("ploadl3"));
        channelParametersEhub.add(new String("plnvreactivel1"));
        channelParametersEhub.add(new String("pinvreactivel2"));
        channelParametersEhub.add(new String("pinvreactivel3"));
        channelParametersEhub.add(new String("ts"));
        channelParametersEhub.add(new String("ploadreactivel1"));
        channelParametersEhub.add(new String("ploadreactivel2"));
        channelParametersEhub.add(new String("ploadreactivel3"));
        channelParametersEhub.add(new String("state"));
        channelParametersEhub.add(new String("wloadprodql1"));
        channelParametersEhub.add(new String("wloadprodql2"));
        channelParametersEhub.add(new String("wloadprodql3"));
        channelParametersEhub.add(new String("ppv"));
        channelParametersEhub.add(new String("pinvl1"));
        channelParametersEhub.add(new String("pinvl2"));
        channelParametersEhub.add(new String("pinvl3"));
        channelParametersEhub.add(new String("iextql1"));
        channelParametersEhub.add(new String("iextql2"));
        channelParametersEhub.add(new String("iextql3"));
        channelParametersEhub.add(new String("pextl1"));
        channelParametersEhub.add(new String("pextl2"));
        channelParametersEhub.add(new String("pextl3"));
        channelParametersEhub.add(new String("wextprodql1"));
        channelParametersEhub.add(new String("wextprodql2"));
        channelParametersEhub.add(new String("wextprodql3"));
        channelParametersEhub.add(new String("wpv"));
        channelParametersEhub.add(new String("pextreactivel1"));
        channelParametersEhub.add(new String("pextreactivel2"));
        channelParametersEhub.add(new String("pextreactivel3"));
        channelParametersEhub.add(new String("udcpos"));
        channelParametersEhub.add(new String("udcneg"));
        channelParametersEhub.add(new String("sext"));
        channelParametersEhub.add(new String("iexdtl1"));
        channelParametersEhub.add(new String("iexdtl2"));
        channelParametersEhub.add(new String("iexdtl3"));
        channelParametersEhub.add(new String("wextconsq_3p"));
        channelParametersEhub.add(new String("ildl1"));
        channelParametersEhub.add(new String("ildl2"));
        channelParametersEhub.add(new String("ildl3"));
        channelParametersEhub.add(new String("gridfreq"));
        channelParametersEhub.add(new String("wloadconsq_3p"));
        channelParametersEhub.add(new String("ull1"));
        channelParametersEhub.add(new String("ull2"));
        channelParametersEhub.add(new String("ull3"));
        channelParametersEhub.add(new String("wextprodq_3p"));
        channelParametersEhub.add(new String("ilql1"));
        channelParametersEhub.add(new String("ilql2"));
        channelParametersEhub.add(new String("ilql3"));
        channelParametersEhub.add(new String("winvprodql1"));
        channelParametersEhub.add(new String("winvprodql2"));
        channelParametersEhub.add(new String("winvprodql3"));
        channelParametersEhub.add(new String("ill1"));
        channelParametersEhub.add(new String("ill2"));
        channelParametersEhub.add(new String("ill3"));
        channelParametersEhub.add(new String("wbatprod"));
        channelParametersEhub.add(new String("wbatcons"));
        channelParametersEhub.add(new String("soc"));
        channelParametersEhub.add(new String("soh"));
        channelParametersEhub.add(new String("pbat"));
        channelParametersEhub.add(new String("ratedcap"));
        return channelParametersEhub;
    }
}
