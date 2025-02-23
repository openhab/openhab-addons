/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.live.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class NumberToBooleanMapper extends XmlAdapter<Integer, Boolean> {

    @Override
    public Boolean unmarshal(Integer v) throws Exception {
        return v == 1 ? true : false;
    }

    @Override
    public Integer marshal(Boolean v) throws Exception {
        return v ? 1 : 0;
    }
}
