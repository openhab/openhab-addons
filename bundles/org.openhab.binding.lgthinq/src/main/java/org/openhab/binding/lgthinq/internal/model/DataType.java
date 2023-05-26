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
package org.openhab.binding.lgthinq.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

/**
 * The {@link DataType} class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DataType {
    private final String name;
    private final boolean isNumeric;
    private final boolean isEnum;
    @Nullable
    private final List<StateOption> options;

    public DataType(String name, boolean isNumeric, boolean isEnum, @Nullable List<StateOption> options) {
        this.name = name;
        this.isNumeric = isNumeric;
        this.isEnum = isEnum;
        this.options = options;
    }

    public @Nullable List<StateOption> getOptions() {
        return options;
    }

    public String getName() {
        return name;
    }

    public boolean isNumeric() {
        return isNumeric;
    }

    public boolean isEnum() {
        return isEnum;
    }
}
