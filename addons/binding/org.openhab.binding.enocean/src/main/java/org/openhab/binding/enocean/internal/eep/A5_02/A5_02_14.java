/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_02;

import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_02_14 extends A5_02 {

    public A5_02_14(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected double getScaledMin() {
        return -20;
    }

    @Override
    protected double getScaledMax() {
        return 60;
    }

}
