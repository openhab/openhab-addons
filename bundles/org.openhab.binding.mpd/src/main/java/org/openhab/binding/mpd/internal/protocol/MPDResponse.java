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
package org.openhab.binding.mpd.internal.protocol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for encapsulating an MPD response
 *
 * @author Stefan Röllin - Initial contribution
 */

@NonNullByDefault
public class MPDResponse {
    private final String command;
    private final List<String> lines = new ArrayList<>();
    private boolean failed = false;

    public MPDResponse(String command) {
        this.command = command;
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public String getCommand() {
        return command;
    }

    public List<String> getLines() {
        return lines;
    }

    public boolean isOk() {
        return !failed;
    }

    public void setFailed() {
        failed = true;
    }
}
