/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RotelThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelThingConfiguration {

    public @NonNullByDefault({}) String serialPort;
    public @NonNullByDefault({}) String host;
    public @NonNullByDefault({}) Integer port;
    public @NonNullByDefault({}) String inputLabelCd;
    public @NonNullByDefault({}) String inputLabelTuner;
    public @NonNullByDefault({}) String inputLabelTape;
    public @NonNullByDefault({}) String inputLabelPhono;
    public @NonNullByDefault({}) String inputLabelVideo1;
    public @NonNullByDefault({}) String inputLabelVideo2;
    public @NonNullByDefault({}) String inputLabelVideo3;
    public @NonNullByDefault({}) String inputLabelVideo4;
    public @NonNullByDefault({}) String inputLabelVideo5;
    public @NonNullByDefault({}) String inputLabelVideo6;
    public @NonNullByDefault({}) String inputLabelUsb;
    public @NonNullByDefault({}) String inputLabelMulti;
    public @NonNullByDefault({}) String protocol;
}
