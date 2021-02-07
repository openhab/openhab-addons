/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * See {@link DeviceListModel}.
 *
 * @author Joshua Bacher - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "state" })
@XmlRootElement(name = "simpleonoff")
public class SimpleOnOffModel {

    public boolean state;

    public static State asState(Boolean state) {
        if (state != null && state == true) {
            return OnOffType.ON;
        } else {
            return OnOffType.OFF;
        }
    }

    public boolean getState() {
        return state;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleOnOffModel{");
        sb.append("state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}
