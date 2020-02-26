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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.exception.HiveApiAuthenticationException;
import org.openhab.binding.hive.internal.client.exception.HiveApiUnknownException;
import org.openhab.binding.hive.internal.client.exception.HiveException;
import org.openhab.binding.hive.internal.client.repository.NodeRepository;
import org.openhab.binding.hive.internal.client.repository.SessionRepository;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class DefaultHiveClientTest {
    @NonNullByDefault({})
    @Mock
    private SessionAuthenticationManager authenticationManager;

    @NonNullByDefault({})
    @Mock
    private SessionRepository sessionRepository;

    @NonNullByDefault({})
    @Mock
    private NodeRepository nodeRepository;

    @NonNullByDefault({})
    private String username;

    @NonNullByDefault({})
    private String password;

    @NonNullByDefault({})
    private Session session;

    @Before
    public void setUp() {
        initMocks(this);
        this.username = "hiveuser@example.com";
        this.password = "password123";
        this.session = new Session(
                new SessionId("deadbeef-dead-beef-dead-beefdeadbeef"),
                new UserId(UUID.fromString("deadbeef-dead-beef-dead-beefdeadbeef"))
        );
    }

    private DefaultHiveClient createClient() throws HiveException {
        return new DefaultHiveClient(
                this.authenticationManager,
                this.username,
                this.password,
                this.sessionRepository,
                this.nodeRepository
        );
    }

    /**
     * Test that DefaultHiveClient tries to authenticate when it is created.
     */
    @Test
    public void testGoodLogin() throws HiveException {
        /* Given */
        when(this.sessionRepository.createSession(any(), any())).thenReturn(this.session);


        /* When */
        final DefaultHiveClient hiveClient = createClient();


        /* Then */
        verify(this.sessionRepository, times(1)).createSession(eq(this.username), eq(this.password));
        verify(this.authenticationManager, times(1)).setSession(eq(this.session));
    }

    /**
     * Test that DefaultHiveClient passes on authentication exceptions.
     */
    @Test(expected = HiveApiAuthenticationException.class)
    public void testBadCredentialsLogin() throws HiveException {
        /* Given */
        when(this.sessionRepository.createSession(any(), any())).thenThrow(new HiveApiAuthenticationException());


        /* When / Then */
        // This should throw HiveApiAuthenticationException.
        // No assertThrows in this version of jUnit :(
        final DefaultHiveClient hiveClient = createClient();
    }

    /**
     * Test that DefaultHiveClient passes on exception when API does something unexpected.
     */
    @Test(expected = HiveApiUnknownException.class)
    public void testApiErrorLogin() throws HiveException {
        /* Given */
        when(this.sessionRepository.createSession(any(), any())).thenThrow(new HiveApiUnknownException());


        /* When / Then */
        // This should throw HiveApiUnknownException.
        // No assertThrows in this version of jUnit :(
        final DefaultHiveClient hiveClient = createClient();
    }

    /**
     * Test the DefaultHiveClient passes on the nodes from the NodeRepository.
     */
    @Test
    public void testGetNodes() throws HiveException {
        /* Given */
        final Node expectedNode = TestUtil.getTestNodeWithFeatures(Collections.emptyMap());
        final Set<Node> expectedResults = Collections.unmodifiableSet(Collections.singleton(expectedNode));
        when(this.nodeRepository.getAllNodes()).thenReturn(expectedResults);

        final DefaultHiveClient hiveClient = createClient();


        /* When */
        Set<Node> returnedResults = hiveClient.getAllNodes();


        /* Then */
        assertThat(returnedResults).containsExactly(expectedNode);
    }
}
