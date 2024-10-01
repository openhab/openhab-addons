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
package org.openhab.binding.emotiva.internal.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A helper class for receiving {@link EmotivaBarNotifyDTO} messages.
 *
 * @author Espen Fossen - Initial contribution
 */
@XmlRootElement(name = "emotivaBarNotify")
public class EmotivaBarNotifyWrapper {

    @XmlAttribute
    private String sequence;

    @XmlAnyElement(lax = true)
    List<Object> tags;

    @SuppressWarnings("unused")
    public EmotivaBarNotifyWrapper() {
    }

    public String getSequence() {
        return sequence;
    }

    public List<Object> getTags() {
        return tags;
    }
}
