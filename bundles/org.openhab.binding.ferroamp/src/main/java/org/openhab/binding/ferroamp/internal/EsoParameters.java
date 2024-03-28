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
 * The {@link EsoParameters} is responsible for all parameters regarded to ESO
 *
 * @author Örjan Backsell - Initial contribution
 * @author Joel Backsell - Defined parameter records
 *
 */

@NonNullByDefault
public class EsoParameters {
    public String jsonPostEso;

    public EsoParameters(String jsonPostEso) {
        this.jsonPostEso = jsonPostEso;
    }

    public static List<String> getChannelParametersEso() {
        final List<String> channelParametersEso = new ArrayList<>();
        channelParametersEso.add(new String("faultcode"));
        channelParametersEso.add(new String("id"));
        channelParametersEso.add(new String("ibat"));
        channelParametersEso.add(new String("ubat"));
        channelParametersEso.add(new String("relaystatus"));
        channelParametersEso.add(new String("soc"));
        channelParametersEso.add(new String("temp"));
        channelParametersEso.add(new String("wbatcons"));
        channelParametersEso.add(new String("wbatprod"));
        channelParametersEso.add(new String("udc"));
        channelParametersEso.add(new String("ts"));
        return channelParametersEso;
    }
}
