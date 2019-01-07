/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel.templates;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlType(name = "device")
public class DeviceModel {

    @XmlAttribute(name = "identifier")
    private String identifier;

    public String getIdentifier() {
        return identifier != null ? identifier.replace(" ", "") : null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identifier", getIdentifier()).toString();
    }
}
