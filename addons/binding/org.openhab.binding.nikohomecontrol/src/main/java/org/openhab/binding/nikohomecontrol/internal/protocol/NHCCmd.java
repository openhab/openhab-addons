/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

/**
 * Class {@link NHCCmd} used as input to gson to send commands to Niko Home Control. Extends {@link NHCBaseMessage}.
 * <p>
 * Example: <code>{"cmd":"executeactions","id":1,"value1":0}</code>
 *
 * @author Mark Herwege
 */
@SuppressWarnings("unused")
class NHCCmd extends NHCBaseMessage {
    private Integer id;
    private Integer value1;
    private Integer value2;
    private Integer value3;
    private Integer startValue;
    private Integer endValue;

    NHCCmd(String cmd) {
        super.setCmd(cmd);
    }

    NHCCmd(String cmd, Integer id, Integer value1) {
        this(cmd);
        this.id = id;
        this.value1 = value1;
    }

    NHCCmd(String cmd, Integer id, Integer value1, Integer value2, Integer value3) {
        this(cmd, id, value1);
        this.value2 = value2;
        this.value3 = value3;
    }

    public void setStartValue(Integer startValue) {
        this.startValue = startValue;
    }

    public void setEndValue(Integer endValue) {
        this.endValue = endValue;
    }

}