/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.config;

import java.math.BigDecimal;

/**
 * Configuration class for PhantomButton thing type.
 * 
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class PhantomButtonConfig {

    private int buttonNumber;
    private BigDecimal fadeSec;

    public int getButtonNumber() {
        return buttonNumber;
    }

    public void setButtonNumber(int buttonNumber) {
        this.buttonNumber = buttonNumber;
    }

    public BigDecimal getFadeSec() {
        return fadeSec;
    }

    public void setFadeSec(BigDecimal fadeSec) {
        this.fadeSec = fadeSec;
    }

}
