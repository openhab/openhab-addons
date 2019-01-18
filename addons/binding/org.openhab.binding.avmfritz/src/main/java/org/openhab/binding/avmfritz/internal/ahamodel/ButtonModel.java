/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlRootElement(name = "button")
@XmlType(propOrder = { "lastpressedtimestamp" })
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
        return new ToStringBuilder(this).append("lastpressedtimestamp", getLastpressedtimestamp()).toString();
    }
}
