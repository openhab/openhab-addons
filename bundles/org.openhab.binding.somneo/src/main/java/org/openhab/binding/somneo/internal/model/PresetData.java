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
package org.openhab.binding.somneo.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the preset state from the API.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class PresetData {

    private static final String LABEL_TEMPLATE = "%s fm";
    private static final String LABEL_EMPTY_TEMPLATE = "FM %s";

    @SerializedName("1")
    private @Nullable String preset1;

    @SerializedName("2")
    private @Nullable String preset2;

    @SerializedName("3")
    private @Nullable String preset3;

    @SerializedName("4")
    private @Nullable String preset4;

    @SerializedName("5")
    private @Nullable String preset5;

    public List<StateOption> createPresetOptions() {
        List<StateOption> stateOptions = new ArrayList<>();
        stateOptions.add(createStateOption("1", preset1));
        stateOptions.add(createStateOption("2", preset2));
        stateOptions.add(createStateOption("3", preset3));
        stateOptions.add(createStateOption("4", preset4));
        stateOptions.add(createStateOption("5", preset5));
        return stateOptions;
    }

    private static StateOption createStateOption(String index, @Nullable String preset) {
        String label;
        if (preset == null || "".equals(preset)) {
            label = String.format(LABEL_EMPTY_TEMPLATE, index);
        } else {
            label = String.format(LABEL_TEMPLATE, preset);
        }
        return new StateOption(index, label);
    }
}
