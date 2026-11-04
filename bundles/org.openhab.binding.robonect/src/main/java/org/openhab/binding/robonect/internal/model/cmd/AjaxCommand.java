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
package org.openhab.binding.robonect.internal.model.cmd;

/**
 * Queries localized status text used by the Robonect web interface.
 *
 * @author Nico Brüttner - Ajax UI compatibility
 */
public class AjaxCommand implements Command {

    private final int type;
    private final int group;

    /**
     * Creates a query for an Ajax UI information group.
     *
     * @param type web-interface response type
     * @param group web-interface response group
     */
    public AjaxCommand(int type, int group) {
        this.type = type;
        this.group = group;
    }

    /**
     * Builds the Ajax request URL for this {@link Command}.
     *
     * @param baseUrl base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseUrl) {
        return baseUrl + "?type=" + type + "&group=" + group;
    }
}
