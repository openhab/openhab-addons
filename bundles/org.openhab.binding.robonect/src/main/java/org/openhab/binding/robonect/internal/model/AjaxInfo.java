/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Localized status text returned by the undocumented Robonect web-interface endpoint.
 *
 * @author Nico Brüttner - Ajax UI compatibility
 */
public class AjaxInfo {

    private @Nullable String tmnucontrol;
    private @Nullable String tmnutimer;

    /**
     * Returns the localized control-menu text.
     *
     * @return localized control-menu text, or {@code null} when it is absent
     */
    public @Nullable String getControlMenuText() {
        return tmnucontrol;
    }

    public void setControlMenuText(@Nullable String controlMenuText) {
        tmnucontrol = controlMenuText;
    }

    /**
     * Returns the localized timer-menu text.
     *
     * @return localized timer-menu text, or {@code null} when it is absent
     */
    public @Nullable String getTimerMenuText() {
        return tmnutimer;
    }

    public void setTimerMenuText(@Nullable String timerMenuText) {
        tmnutimer = timerMenuText;
    }
}
