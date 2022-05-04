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
