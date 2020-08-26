/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * The Send Elk Command class, to put the elk into armed away mode.
 *
 * @author Matt Myers - Initial Contribution
 *
 */
public class ControlOutputOn extends ElkMessage {
    private final int outputno;
    private final int time;

    public ControlOutputOn(String command) {
        super(ElkCommand.ControlOutputOn);
        String strOutputno = command.substring(2, 5);
        String strTime = command.substring(5, 10);

        int outputno = Integer.valueOf(strOutputno);
        int time = Integer.valueOf(strTime);

        if (outputno > 208) {
            outputno = 208;
        }
        if (outputno < 0) {
            outputno = 0;
        }
        this.outputno = outputno;

        if (time > 65535) {
            time = 65535;
        }
        if (time < 0) {
            time = 0;
        }
        this.time = time;
    }

    @Override
    public String getData() {
        return String.format("%03d", outputno) + String.format("%05d", time);
    }
}
