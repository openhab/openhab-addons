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
package org.openhab.binding.hive.internal.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class LinkTest {
    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(Link.class)
                // Value field is not allowed to be null.
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @Test
    public void testGetters() {
        /* Given */
        final NodeId originalNodeId = new NodeId(UUID.randomUUID());
        final GroupId originalGroupId = GroupId.TRVS;

        final Link link = new Link(
                originalNodeId,
                originalGroupId
        );


        /* When */
        final NodeId gotNodeId = link.getNodeId();
        final GroupId gotGroupId = link.getGroupId();


        /* Then */
        assertThat(gotNodeId).isEqualTo(originalNodeId);
        assertThat(gotGroupId).isEqualTo(originalGroupId);
    }
}
