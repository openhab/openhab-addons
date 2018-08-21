/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.config;

import java.math.BigDecimal;

/**
 * The {@link Ihc2NumberThingConfig} holds the settings used in the
 * dynamic state description.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */

public class Ihc2NumberThingConfig extends Ihc2PatternThingConfig {
    private BigDecimal minimum;
    private BigDecimal maximum;
    private BigDecimal step;

    public BigDecimal getMinimum() {
        return minimum;
    }

    public void setMinimum(BigDecimal minimum) {
        this.minimum = minimum;
    }

    public BigDecimal getMaximum() {
        return maximum;
    }

    public void setMaximum(BigDecimal maximum) {
        this.maximum = maximum;
    }

    public BigDecimal getStep() {
        return step;
    }

    public void setStep(BigDecimal step) {
        this.step = step;
    }

}
