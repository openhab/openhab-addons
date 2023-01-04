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
package org.openhab.binding.pushsafer.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ParameterOption;

/**
 * The {@link Icons} is the Java class used to map the JSON response to a Pushsafer API request..
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@NonNullByDefault
public class Icon {
    public String icon;
    public String label;

    public Icon(String icon, String label) {
        this.icon = icon;
        this.label = label;
    }

    public ParameterOption getAsParameterOption() {
        return new ParameterOption(icon, label);
    }
}
