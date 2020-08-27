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
 * The arm away class, to put the elk into armed away mode.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ArmToStayHome extends ElkMessage {
    private int area;
    private String pincode;

    public ArmToStayHome(int area, String pincode) {
        super(ElkCommand.ArmToStayHome);
        this.area = area;
        this.pincode = pincode;
        if (area > 8) {
            this.area = 8;
        }
        if (area < 0) {
            this.area = 0;
        }
        if (pincode.length() == 4) {
            this.pincode = "00" + pincode;
        }
    }

    @Override
    public String getData() {
        return this.area + this.pincode;
    }
}
