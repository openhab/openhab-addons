/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.Base;

import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class _RPSMessage extends EEP {

    protected boolean t21;
    protected boolean nu;

    public static final byte T21Flag = 0x20;
    public static final byte NUFlag = 0x10;

    public _RPSMessage() {
        super();
    }

    public _RPSMessage(ERP1Message packet) {
        super(packet);
    }

    public boolean isT21() {
        return t21;
    }

    public boolean isNU() {
        return nu;
    }

    @Override
    public EEP setStatus(byte status) {
        super.setStatus(status);
        t21 = (status & T21Flag) != 0;
        nu = (status & NUFlag) != 0;

        return this;
    }
}
