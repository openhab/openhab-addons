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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacMetadataDevice {

    private String name = "";

    private String addr = "";

    private String type = "unknown";

    private String serialNr = "";

    private @Nullable String treeDate;

    private @Nullable String treeTime;

    private boolean treeGenerated;
    private int treeId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerialNr() {
        return serialNr;
    }

    public void setSerialNr(String serialNr) {
        this.serialNr = serialNr;
    }

    public @Nullable String getTreeDate() {
        return treeDate;
    }

    public void setTreeDate(String treeDate) {
        this.treeDate = treeDate;
    }

    public @Nullable String getTreeTime() {
        return treeTime;
    }

    public void setTreeTime(String treeTime) {
        this.treeTime = treeTime;
    }

    public boolean getTreeGenerated() {
        return treeGenerated;
    }

    public void setTreeGenerated(boolean treeGenerated) {
        this.treeGenerated = treeGenerated;
    }

    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }
}
