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
package org.openhab.binding.homeconnect.internal.client.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Program model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class Program {
    private final String key;
    private final List<Option> options;

    public Program(String key, List<Option> options) {
        this.key = key;
        this.options = options;
    }

    public String getKey() {
        return key;
    }

    public List<Option> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "Program [key=" + key + ", options=" + options + "]";
    }
}
