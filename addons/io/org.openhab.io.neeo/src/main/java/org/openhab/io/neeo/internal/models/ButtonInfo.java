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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * This model simply contains information about a button. A button has a label and a suffix. The label is used to
 * identify the name and the label used by NEEO Brain. The suffix is what is appended to the button action URL to
 * identify the action required when the button is pressed.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class ButtonInfo {
    private final String label;
    private final String suffix;

    /** Represents a power ON button */
    public static final ButtonInfo POWERON = new ButtonInfo("POWER ON", "on");
    /** Represents a power OFF button */
    public static final ButtonInfo POWEROFF = new ButtonInfo("POWER OFF", "off");

    /**
     * Creates the button information from the given label and suffix.
     *
     * @param label a non-null, non-empty label
     * @param suffix a non-null, non-empty suffix
     */
    public ButtonInfo(final String label, final String suffix) {
        NeeoUtil.requireNotEmpty("label", "label cannot be null or empty");
        NeeoUtil.requireNotEmpty("suffix", "suffix cannot be null or empty");

        this.label = label;
        this.suffix = suffix;
    }

    /**
     * Returns the label used for this button
     *
     * @return a non-null, non-empty label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the suffix used for this button
     *
     * @return a non-null, non-empty suffix
     */
    public String getSuffix() {
        return suffix;
    }
}
