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
