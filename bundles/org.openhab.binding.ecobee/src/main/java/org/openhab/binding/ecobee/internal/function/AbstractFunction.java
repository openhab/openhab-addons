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
package org.openhab.binding.ecobee.internal.function;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * The {@link AbstractFunction} defines the base class used by all
 * thermostat functions.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractFunction {

    @Expose(serialize = false)
    protected static final DateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");

    @Expose(serialize = false)
    protected static final DateFormat HMS = new SimpleDateFormat("HH:mm:ss");

    public String type;

    public Map<String, Object> params;

    public AbstractFunction(String type) {
        this.type = type;
        this.params = new HashMap<>();
    }
}
