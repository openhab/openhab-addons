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
package org.openhab.binding.avmfritz.internal.dto.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openhab.core.types.CommandOption;

/**
 * See {@link TemplateListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
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

    public CommandOption toCommandOption() {
        return new CommandOption(getIdentifier(), getName());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[identifier=").append(identifier).append(",id=").append(templateId)
                .append(",functionbitmask=").append(functionbitmask).append(",applymask=").append(applymask)
                .append(",name=").append(name).append(",devices=").append(deviceList).append(",applymasks=")
                .append(applyMaskList).append("]").toString();
    }
}
