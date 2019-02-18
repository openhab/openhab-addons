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
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link AVMFritzBaseModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
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
        return new ToStringBuilder(this).append(super.toString()).append(getGroupinfo()).toString();
    }

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
            return new ToStringBuilder(this).append("masterdeviceid", getMasterdeviceid())
                    .append("members", getMembers()).toString();
        }
    }
}
