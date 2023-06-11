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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.core.Is.is;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMResourceName.SDMResourceNameType;

/**
 * Tests the data provided by {@link org.openhab.binding.nest.internal.sdm.dto.SDMResourceName}
 * based on resource name strings.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMResourceNameTest {

    @Test
    public void nameless() {
        SDMResourceName resourceName = SDMResourceName.NAMELESS;
        assertThat(resourceName.name, is(emptyString()));
        assertThat(resourceName.projectId, is(emptyString()));
        assertThat(resourceName.deviceId, is(emptyString()));
        assertThat(resourceName.structureId, is(emptyString()));
        assertThat(resourceName.roomId, is(emptyString()));
        assertThat(resourceName.type, is(SDMResourceNameType.UNKNOWN));
    }

    @Test
    public void deviceName() {
        String name = "enterprises/project-id/devices/device-id";

        SDMResourceName resourceName = new SDMResourceName(name);
        assertThat(resourceName.name, is(name));
        assertThat(resourceName.projectId, is("project-id"));
        assertThat(resourceName.deviceId, is("device-id"));
        assertThat(resourceName.structureId, is(emptyString()));
        assertThat(resourceName.roomId, is(emptyString()));
        assertThat(resourceName.type, is(SDMResourceNameType.DEVICE));
    }

    @Test
    public void structureName() {
        String name = "enterprises/project-id/structures/structure-id";

        SDMResourceName resourceName = new SDMResourceName(name);
        assertThat(resourceName.name, is(name));
        assertThat(resourceName.projectId, is("project-id"));
        assertThat(resourceName.deviceId, is(emptyString()));
        assertThat(resourceName.structureId, is("structure-id"));
        assertThat(resourceName.roomId, is(emptyString()));
        assertThat(resourceName.type, is(SDMResourceNameType.STRUCTURE));
    }

    @Test
    public void roomName() {
        String name = "enterprises/project-id/structures/structure-id/rooms/room-id";

        SDMResourceName resourceName = new SDMResourceName(name);
        assertThat(resourceName.name, is(name));
        assertThat(resourceName.projectId, is("project-id"));
        assertThat(resourceName.deviceId, is(emptyString()));
        assertThat(resourceName.structureId, is("structure-id"));
        assertThat(resourceName.roomId, is("room-id"));
        assertThat(resourceName.type, is(SDMResourceNameType.ROOM));
    }
}
