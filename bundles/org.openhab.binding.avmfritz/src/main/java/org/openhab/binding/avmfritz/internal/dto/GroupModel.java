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
package org.openhab.binding.avmfritz.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * See {@link AVMFritzBaseModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "group")
public class GroupModel extends AVMFritzBaseModel {

    private GroupInfoModel groupinfo;

    public GroupInfoModel getGroupinfo() {
        return groupinfo;
    }

    public void setGroupinfo(GroupInfoModel groupinfo) {
        this.groupinfo = groupinfo;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(groupinfo).append("]").toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = { "masterdeviceid", "members" })
    public static class GroupInfoModel {
        private String masterdeviceid;
        private String members;

        public String getMasterdeviceid() {
            return masterdeviceid;
        }

        public void setMasterdeviceid(String masterdeviceid) {
            this.masterdeviceid = masterdeviceid;
        }

        public String getMembers() {
            return members;
        }

        public void setMembers(String members) {
            this.members = members;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("[masterdeviceid=").append(masterdeviceid).append(",members=")
                    .append(members).append("]").toString();
        }
    }
}
