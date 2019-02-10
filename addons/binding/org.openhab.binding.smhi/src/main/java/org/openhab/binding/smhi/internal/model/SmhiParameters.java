/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.smhi.internal.model;

/**
 * The {@link SmhiParameters} is the Java class used to map the JSON response to an SMHI
 * request.
 *
 * @author Michael Parment - Initial contribution
 */

public class SmhiParameters {
    private String unit;

    private String levelType;

    public Double[] values;

    private String level;

    public String name;

    public String getUnit() {
        return unit;
    }

    public String getLevelType() {
        return levelType;
    }

    public Double[] getValues() {
        return values;
    }

    public String getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ClassPojo [unit = " + unit + ", levelType = " + levelType + ", values = " + values + ", level = "
                + level + ", name = " + name + "]";
    }
}
