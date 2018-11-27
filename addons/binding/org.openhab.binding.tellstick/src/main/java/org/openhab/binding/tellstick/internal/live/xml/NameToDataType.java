/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
