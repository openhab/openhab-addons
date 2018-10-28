/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
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
