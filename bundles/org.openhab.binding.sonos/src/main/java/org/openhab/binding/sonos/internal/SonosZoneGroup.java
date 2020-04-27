/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sonos.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The {@link SonosZoneGroup} is data structure to describe
 * Groups of Zone Players in the Sonos ecosystem
 *
 * @author Karel Goderis - Initial contribution
 */
public class SonosZoneGroup implements Cloneable {

    private final List<String> members;
    private List<String> memberZoneNames;
    private final String coordinator;
    private final String id;

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    public SonosZoneGroup(String id, String coordinator, Collection<String> members,
            Collection<String> memberZoneNames) {
        this.members = new ArrayList<>(members);
        if (!this.members.contains(coordinator)) {
            this.members.add(coordinator);
        }
        this.memberZoneNames = new ArrayList<>(memberZoneNames);
        this.coordinator = coordinator;
        this.id = id;
    }

    public SonosZoneGroup(String id, String coordinator, Collection<String> members) {
        this.members = new ArrayList<>(members);
        if (!this.members.contains(coordinator)) {
            this.members.add(coordinator);
        }
        this.coordinator = coordinator;
        this.id = id;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<String> getMemberZoneNames() {
        return memberZoneNames;
    }

    public String getCoordinator() {
        return coordinator;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof SonosZoneGroup) {
            SonosZoneGroup group = (SonosZoneGroup) obj;
            return group.getId().equals(getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
