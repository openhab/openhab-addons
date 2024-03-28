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
 * The {@link SsoParameters} is responsible for all parameters regarded to SSO
 *
 * @author Örjan Backsell - Initial contribution
 * @author Joel Backsell - Defined parameter records
 *
 */

@NonNullByDefault
public class SsoParameters {

    public static List<String> getChannelParametersSso() {
        final List<String> channelParametersSso = new ArrayList<>();
        channelParametersSso.add(new String("relaystatus"));
        channelParametersSso.add(new String("temp"));
        channelParametersSso.add(new String("wpv"));
        channelParametersSso.add(new String("ts"));
        channelParametersSso.add(new String("udc"));
        channelParametersSso.add(new String("faultcode"));
        channelParametersSso.add(new String("ipv"));
        channelParametersSso.add(new String("upv"));
        channelParametersSso.add(new String("id"));
        return channelParametersSso;
    }
}
