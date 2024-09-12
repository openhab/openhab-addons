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
package org.openhab.binding.tellstick.internal.live.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class NameToDataType extends XmlAdapter<String, LiveDataType> {
    @Override
    public LiveDataType unmarshal(String v) throws Exception {
        return LiveDataType.fromName(v);
    }

    @Override
    public String marshal(LiveDataType v) throws Exception {
        return v.toString();
    }
}
