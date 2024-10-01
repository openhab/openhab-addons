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
package org.openhab.binding.siemenshvac.internal.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacMetadataMenu extends SiemensHvacMetadata {
    private LinkedHashMap<Integer, SiemensHvacMetadata> childList;

    public SiemensHvacMetadataMenu() {
        childList = new LinkedHashMap<Integer, SiemensHvacMetadata>();
    }

    public void addChild(SiemensHvacMetadata information) {
        childList.put(information.getId(), information);
    }

    public HashMap<Integer, SiemensHvacMetadata> getChilds() {
        return this.childList;
    }

    public boolean hasChild(int Id) {
        return this.childList.containsKey(Id);
    }

    public @NotNull SiemensHvacMetadata getChild(int Id) {
        return this.childList.get(Id);
    }
}
