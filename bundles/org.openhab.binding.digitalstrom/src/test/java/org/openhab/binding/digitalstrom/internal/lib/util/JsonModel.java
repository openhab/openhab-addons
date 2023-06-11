/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Model used in test cases.
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@NonNullByDefault
public class JsonModel {
    public JsonModel(List<OutputChannel> outputChannels) {
        this(-1, outputChannels);
    }

    public JsonModel(int outputMode, List<OutputChannel> outputChannels) {
        this.outputMode = outputMode;
        this.outputChannels = new ArrayList<>();
        if (outputChannels != null) {
            this.outputChannels = outputChannels;
        }
    }

    int outputMode;

    List<OutputChannel> outputChannels;
}
