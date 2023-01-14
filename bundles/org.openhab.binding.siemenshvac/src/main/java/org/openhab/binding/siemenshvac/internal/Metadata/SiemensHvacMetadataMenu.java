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
package org.openhab.binding.siemenshvac.internal.Metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacMetadataMenu extends SiemensHvacMetadata {
    private LinkedHashMap<String, SiemensHvacMetadata> childList;

    public SiemensHvacMetadataMenu() {
        childList = new LinkedHashMap<String, SiemensHvacMetadata>();
    }

    public void AddChild(SiemensHvacMetadata information) {
        childList.put(information.getLongDesc(), information);
    }

    public HashMap<String, SiemensHvacMetadata> getChilds() {
        return this.childList;
    }
}
