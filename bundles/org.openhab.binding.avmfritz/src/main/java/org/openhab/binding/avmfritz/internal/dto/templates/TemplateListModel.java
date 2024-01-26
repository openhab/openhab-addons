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

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This JAXB model class maps the XML response to an <b>gettemplatelistinfos</b> command on a FRITZ!Box device.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "templatelist")
public class TemplateListModel {

    @XmlAttribute(name = "version")
    private String version;

    @XmlElement(name = "template")
    private List<TemplateModel> templates;

    public List<TemplateModel> getTemplates() {
        if (templates == null) {
            templates = Collections.emptyList();
        }
        return templates;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[templates=").append(templates).append(",version=").append(version)
                .append("]").toString();
    }
}
