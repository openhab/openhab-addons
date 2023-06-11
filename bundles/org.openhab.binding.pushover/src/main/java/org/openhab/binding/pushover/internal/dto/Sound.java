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
package org.openhab.binding.pushover.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ParameterOption;

/**
 * The {@link Sound} is the Java class used to map the JSON response to a Pushover API request..
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class Sound {
    public String sound;
    public String label;

    public Sound(String sound, String label) {
        this.sound = sound;
        this.label = label;
    }

    public ParameterOption getAsParameterOption() {
        return new ParameterOption(sound, label);
    }
}
