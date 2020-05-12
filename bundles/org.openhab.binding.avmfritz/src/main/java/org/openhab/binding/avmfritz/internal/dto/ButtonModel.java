/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "lastpressedtimestamp" })
@XmlRootElement(name = "button")
public class ButtonModel {

    private int lastpressedtimestamp;

    public int getLastpressedtimestamp() {
        return lastpressedtimestamp;
    }

    public void setLastpressedtimestamp(int lastpressedtimestamp) {
        this.lastpressedtimestamp = lastpressedtimestamp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[lastpressedtimestamp=").append(lastpressedtimestamp).append("]").toString();
    }
}
