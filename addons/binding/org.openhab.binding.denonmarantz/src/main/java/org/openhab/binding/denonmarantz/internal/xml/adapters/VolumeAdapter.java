/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.xml.adapters;

import static org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants.DB_OFFSET;

import java.math.BigDecimal;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Maps Denon volume values in db to percentage
 *
 * @author Jeroen Idserda - Initial contribution
 */
public class VolumeAdapter extends XmlAdapter<String, BigDecimal> {

    @Override
    public BigDecimal unmarshal(String v) throws Exception {
        if (v != null && !v.trim().equals("--")) {
            return new BigDecimal(v.trim()).add(DB_OFFSET);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String marshal(BigDecimal v) throws Exception {
        if (v.equals(BigDecimal.ZERO)) {
            return "--";
        }

        return v.subtract(DB_OFFSET).toString();
    }
}
