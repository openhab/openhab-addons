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
 * Maps 'On' and 'Off' string values to a boolean
 *
 * @author Jeroen Idserda - Initial contribution
 */
@NonNullByDefault
public class OnOffAdapter extends XmlAdapter<@Nullable String, @Nullable Boolean> {

    @Override
    public @Nullable Boolean unmarshal(@Nullable String v) throws Exception {
        if (v != null) {
            return Boolean.valueOf("on".equals(v.toLowerCase()));
        }

        return Boolean.FALSE;
    }

    @Override
    public @Nullable String marshal(@Nullable Boolean v) throws Exception {
        return v ? "On" : "Off";
    }
}
