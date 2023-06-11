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
package org.openhab.binding.tacmi.internal.schema;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ChangerX2Entry} class contains mapping information for a changerX2 entry of
 * the API page element
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class ChangerX2Entry {

    public static final String NUMBER_MIN = "min";
    public static final String NUMBER_MAX = "max";
    public static final String NUMBER_STEP = "step";

    static enum OptionType {
        NUMBER,
        SELECT,
    }

    /**
     * field name of the address
     */
    public final String addressFieldName;

    /**
     * The address these options are for
     */
    public final String address;

    /**
     * option type
     */
    public final OptionType optionType;

    /**
     * field name of the option value
     */
    public final String optionFieldName;

    /**
     * the valid options
     */
    public final Map<String, @Nullable String> options;

    public ChangerX2Entry(String addressFieldName, String address, String optionFieldName, OptionType optionType,
            Map<String, @Nullable String> options) {
        this.addressFieldName = addressFieldName;
        this.address = address;
        this.optionFieldName = optionFieldName;
        this.optionType = optionType;
        this.options = options;
    }
}
