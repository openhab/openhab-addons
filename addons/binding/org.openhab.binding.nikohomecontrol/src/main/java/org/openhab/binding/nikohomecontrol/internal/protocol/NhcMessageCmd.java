/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol;

/**
 * Class {@link NhcMessageCmd} used as input to gson to send commands to Niko Home Control. Extends
 * {@link NhcMessageBase}.
 * <p>
 * Example: <code>{"cmd":"executeactions","id":1,"value1":0}</code>
 *
 * @author Mark Herwege - Initial Contribution
 */
@SuppressWarnings("unused")
class NhcMessageCmd extends NhcMessageBase {

    private Integer id;
    private Integer value1;
    private Integer value2;
    private Integer value3;
    private Integer mode;
    private Integer overrule;
    private String overruletime;

    NhcMessageCmd(String cmd) {
        super.setCmd(cmd);
    }

    NhcMessageCmd(String cmd, Integer id) {
        this(cmd);
        this.id = id;
    }

    NhcMessageCmd(String cmd, Integer id, Integer value1) {
        this(cmd, id);
        this.value1 = value1;
    }

    NhcMessageCmd(String cmd, Integer id, Integer value1, Integer value2, Integer value3) {
        this(cmd, id, value1);
        this.value2 = value2;
        this.value3 = value3;
    }

    NhcMessageCmd withMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    NhcMessageCmd withOverrule(Integer overrule) {
        this.overrule = overrule;
        return this;
    }

    NhcMessageCmd withOverruletime(String overruletime) {
        this.overruletime = overruletime;
        return this;
    }
}
