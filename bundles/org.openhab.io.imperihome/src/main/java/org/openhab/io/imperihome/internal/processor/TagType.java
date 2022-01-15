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
package org.openhab.io.imperihome.internal.processor;

/**
 * ISS tag types enumeration.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public enum TagType {

    LABEL("label", false),
    ROOM("room", false),
    TYPE("type", false),
    MAPPING("mapping", false),
    LINK("link", true),
    UNIT("unit", false),
    INVERT("invert", false),
    ICON("icon", false),

    STEP("step", false),
    MIN_VAL("minVal", false),
    MAX_VAL("maxVal", false),
    MODES("modes", false);

    private final String prefix;
    private final boolean multiValue;

    TagType(String prefix, boolean multiValue) {
        this.prefix = prefix;
        this.multiValue = multiValue;
    }

    /**
     * @return Tag prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return True if this tag may exist multiple times on a single item.
     */
    public boolean isMultiValue() {
        return multiValue;
    }
}
