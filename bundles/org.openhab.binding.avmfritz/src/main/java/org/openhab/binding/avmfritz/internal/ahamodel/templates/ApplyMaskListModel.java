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

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@ TemplateModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlRootElement(name = "applymask")
public class ApplyMaskListModel {

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
