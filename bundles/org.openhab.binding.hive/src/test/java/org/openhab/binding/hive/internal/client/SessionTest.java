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

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class SessionTest {
    final SessionId sessionId = new SessionId("my-session-id");
    final UserId userId = new UserId(UUID.randomUUID());

    @Test
    public void testGetters() {
        /* Given */


        /* When */
        final Session session = new Session(sessionId, userId);


        /* Then */
        assertThat(session.getSessionId()).isEqualTo(sessionId);
        assertThat(session.getUserId()).isEqualTo(userId);

        assertThat(session.toString()).isNotNull();
    }

    @Test
    public void testToString() {
        /* Given */
        final Session session = new Session(sessionId, userId);

        /* When */

        final String stringValue = session.toString();

        /* Then */
        assertThat(stringValue).isNotNull();
    }

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(Session.class)
                .verify();
    }
}
