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
package org.openhab.binding.evohome.internal.api.models.v2.dto.request;

/**
 * Builder for mode API requests
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class ModeBuilder extends TimedRequestBuilder<Mode> {

    private String mode;
    private boolean hasSetMode;

    /**
     * Creates a new mode command
     *
     * @return A mode command or null when the configuration is invalid
     *
     */
    @Override
    public Mode build() {
        if (hasSetMode) {
            if (useEndTime()) {
                return new Mode(mode, getYear(), getMonth(), getDay());
            } else {
                return new Mode(mode);
            }
        }
        return null;
    }

    public ModeBuilder setMode(String mode) {
        this.hasSetMode = true;
        this.mode = mode;
        return this;
    }
}
