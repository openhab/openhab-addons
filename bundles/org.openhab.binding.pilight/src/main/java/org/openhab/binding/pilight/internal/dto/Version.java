/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pilight.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * pilight version information object
 *
 * {@link http://www.pilight.org/development/api/#controller}
 *
 * @author Niklas Dörfler - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Version {

    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
