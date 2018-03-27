/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

/**
 * Class {@link NhcMessageCmd} used as input to gson to send commands to Niko Home Control. Extends
 * {@link NhcMessageBase}.
 * <p>
 * Example: <code>{"cmd":"executeactions","id":1,"value1":0}</code>
 *
 * @author Mark Herwege
 */
@SuppressWarnings("unused")
class NhcMessageCmd extends NhcMessageBase {

    private Integer id;
    private Integer value1;
    private Integer value2;
    private Integer value3;
    private Integer startValue;
    private Integer endValue;

    NhcMessageCmd(String cmd) {
        super.setCmd(cmd);
    }

    NhcMessageCmd(String cmd, Integer id, Integer value1) {
        this(cmd);
        this.id = id;
        this.value1 = value1;
    }

    NhcMessageCmd(String cmd, Integer id, Integer value1, Integer value2, Integer value3) {
        this(cmd, id, value1);
        this.value2 = value2;
        this.value3 = value3;
    }

    void setStartValue(Integer startValue) {
        this.startValue = startValue;
    }

    void setEndValue(Integer endValue) {
        this.endValue = endValue;
    }
}
