/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api.models.v2.request;

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
