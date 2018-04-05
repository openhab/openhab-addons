/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    private Groupinfo groupinfo;

    public Groupinfo getGroupinfo() {
        return groupinfo;
    }

    public void setGroupinfo(Groupinfo groupinfo) {
        this.groupinfo = groupinfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(super.toString()).append(getGroupinfo()).toString();
    }

    @XmlType(propOrder = { "masterdeviceid", "members" })
    public static class Groupinfo {

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
