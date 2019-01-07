/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel.templates;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This JAXB model class maps the XML response to an <b>gettemplatelistinfos</b> command on a FRITZ!Box device.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
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
}
