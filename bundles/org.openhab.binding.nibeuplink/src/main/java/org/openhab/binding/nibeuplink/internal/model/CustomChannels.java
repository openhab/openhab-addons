/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.model;

/**
 * the custom channels which can be configured via config-file
 *
 * @author Alexander Friese - initial contribution
 */
public final class CustomChannels extends AbstractChannels {

    /**
     * singleton
     */
    private static final CustomChannels INSTANCE = new CustomChannels();

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    public static CustomChannels getInstance() {
        return INSTANCE;
    }

    /**
     * singleton should not be instantiated from outside
     */
    private CustomChannels() {
    }

    // Custom Channels
    public static final CustomChannel CH_CH01 = INSTANCE.addChannel(new CustomChannel("CH01", "Custom Channel #01"));
    public static final CustomChannel CH_CH02 = INSTANCE.addChannel(new CustomChannel("CH02", "Custom Channel #02"));
    public static final CustomChannel CH_CH03 = INSTANCE.addChannel(new CustomChannel("CH03", "Custom Channel #03"));
    public static final CustomChannel CH_CH04 = INSTANCE.addChannel(new CustomChannel("CH04", "Custom Channel #04"));
    public static final CustomChannel CH_CH05 = INSTANCE.addChannel(new CustomChannel("CH05", "Custom Channel #05"));
    public static final CustomChannel CH_CH06 = INSTANCE.addChannel(new CustomChannel("CH06", "Custom Channel #06"));
    public static final CustomChannel CH_CH07 = INSTANCE.addChannel(new CustomChannel("CH07", "Custom Channel #07"));
    public static final CustomChannel CH_CH08 = INSTANCE.addChannel(new CustomChannel("CH08", "Custom Channel #08"));
}
