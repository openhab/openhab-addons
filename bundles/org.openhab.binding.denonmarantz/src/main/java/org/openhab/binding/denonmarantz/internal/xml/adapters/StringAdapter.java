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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Adapter to clean up string values
 *
 * @author Jeroen Idserda - Initial contribution
 */
@NonNullByDefault
public class StringAdapter extends XmlAdapter<@Nullable String, @Nullable String> {

    @Override
    public @Nullable String unmarshal(@Nullable String v) throws Exception {
        String val = v;
        if (val != null) {
            return val.trim();
        }
        return val;
    }

    @Override
    public @Nullable String marshal(@Nullable String v) throws Exception {
        return v;
    }
}
