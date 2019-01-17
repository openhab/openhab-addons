/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    private final int area;
    private final String pincode;

    public ArmToStayHome(int area, String pincode) {
        super(ElkCommand.ArmToStayHome);
        this.area = area;
        if (area > 8) {
            area = 8;
        }
        if (area < 0) {
            area = 0;
        }
        if (pincode.length() == 4) {
            pincode = "00" + pincode;
        }
        this.pincode = pincode;
    }

    @Override
    public String getData() {
        return area + pincode;
    }
}
