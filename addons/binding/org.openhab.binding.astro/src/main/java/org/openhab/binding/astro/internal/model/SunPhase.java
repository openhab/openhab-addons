/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Holds the calculated sun phase informations.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SunPhase {
    private SunPhaseName name;

    /**
     * Returns the sun phase.
     */
    public SunPhaseName getName() {
        return name;
    }

    /**
     * Sets the sun phase.
     */
    public void setName(SunPhaseName name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", name).toString();
    }

}
