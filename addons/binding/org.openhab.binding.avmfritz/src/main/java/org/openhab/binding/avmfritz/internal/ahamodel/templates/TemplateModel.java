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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link TemplateListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlType(name = "template")
public class TemplateModel {

    @XmlAttribute(name = "identifier")
    private String identifier;

    @XmlAttribute(name = "id")
    private String templateId;

    @XmlAttribute(name = "functionbitmask")
    private int functionbitmask;

    @XmlAttribute(name = "applymask")
    private int applymask;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "devices")
    private DeviceListModel deviceList;

    @XmlElement(name = "applymask")
    private ApplyMaskListModel applyMaskList;

    public String getIdentifier() {
        return identifier != null ? identifier.replace(" ", "") : null;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public DeviceListModel getDeviceList() {
        return deviceList;
    }

    public ApplyMaskListModel getApplyMaskList() {
        return applyMaskList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identifier", getIdentifier()).append("id", getTemplateId())
                .append("functionbitmask", functionbitmask).append("applymask", applymask).append("name", getName())
                .append("devices", getDeviceList()).append("applymasks", getApplyMaskList()).toString();
    }
}
