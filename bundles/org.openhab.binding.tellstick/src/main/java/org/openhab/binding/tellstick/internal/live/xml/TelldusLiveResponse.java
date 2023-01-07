/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.live.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used to deserialize XML from Telldus Live.
 *
 * @author Jarle Hjortland - Initial contribution
 */
@XmlRootElement(name = "device")
public class TelldusLiveResponse {
    @XmlElement
    public String status;
    @XmlElement
    public String error;

    @Override
    public String toString() {
        return "TelldusLiveResponse [status=" + status + ", error=" + error + "]";
    }
}
