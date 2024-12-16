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
package org.openhab.binding.lgthinq.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ParameterOption;

/**
 * The {@link DeviceParameter} class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DeviceParameter {

    private final String name;
    private final Type type;
    private final String label;
    private final String description;
    private final String defaultValue;
    private DeviceParameterGroup group = new DeviceParameterGroup("", "");
    private final List<ParameterOption> options = new ArrayList<ParameterOption>();

    private final boolean isReadOnly;

    public DeviceParameter(String name, Type type, String label, String description, String defaultValue,
            List<ParameterOption> options, boolean isReadOnly) {
        this.name = name;
        this.type = type;
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
        this.options.addAll(options);
        this.isReadOnly = isReadOnly;
    }

    public DeviceParameterGroup getGroup() {
        return group;
    }

    public void setGroup(DeviceParameterGroup group) {
        this.group = group;
    }

    public List<ParameterOption> getOptions() {
        return options;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
