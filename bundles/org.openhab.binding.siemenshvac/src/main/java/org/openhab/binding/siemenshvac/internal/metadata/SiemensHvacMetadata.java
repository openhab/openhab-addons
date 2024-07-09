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
public class SiemensHvacMetadata {
    private int id = -1;
    private int subId = -1;
    private int groupId = -1;
    private int catId = -1;
    private String shortDescEn = "";
    private String longDescEn = "";
    private String shortDesc = "";
    private String longDesc = "";
    @Nullable
    private transient SiemensHvacMetadata parent;

    public SiemensHvacMetadata() {
    }

    public int getId() {
        return id;
    }

    public void setId(int Id) {
        this.id = Id;
    }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public String getShortDescEn() {
        return shortDescEn;
    }

    public void setShortDescEn(String shortDesc) {
        this.shortDescEn = shortDesc;
    }

    public String getLongDescEn() {
        return longDescEn;
    }

    public void setLongDescEn(String longDesc) {
        this.longDescEn = longDesc;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public @Nullable SiemensHvacMetadata getParent() {
        return parent;
    }

    public void setParent(@Nullable SiemensHvacMetadata parent) {
        this.parent = parent;
    }
}
