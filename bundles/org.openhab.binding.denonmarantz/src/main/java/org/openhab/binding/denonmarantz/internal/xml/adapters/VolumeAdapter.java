/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal.xml.adapters;

import static org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants.DB_OFFSET;

import java.math.BigDecimal;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Maps Denon volume values in dB to percentage
 *
 * @author Jeroen Idserda - Initial contribution
 */
@NonNullByDefault
public class VolumeAdapter extends XmlAdapter<@Nullable String, BigDecimal> {

    @Override
    public BigDecimal unmarshal(@Nullable String v) throws Exception {
        if (v != null && !"--".equals(v.trim())) {
            return new BigDecimal(v.trim()).add(DB_OFFSET);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public @Nullable String marshal(BigDecimal v) throws Exception {
        if (v.equals(BigDecimal.ZERO)) {
            return "--";
        }

        return v.subtract(DB_OFFSET).toString();
    }
}
