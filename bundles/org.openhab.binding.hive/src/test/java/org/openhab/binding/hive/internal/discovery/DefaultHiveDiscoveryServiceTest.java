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
package org.openhab.binding.hive.internal.discovery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.HiveBindingConstants;
import org.openhab.binding.hive.internal.MultithreadedTestBase;


/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class DefaultHiveDiscoveryServiceTest extends MultithreadedTestBase {
    private static final ThingUID BRIDGE_UID = new ThingUID(HiveBindingConstants.THING_TYPE_ACCOUNT, "mybridge");

    @NonNullByDefault({})
    @Mock
    private DiscoveryListener discoveryListener;

    public DefaultHiveDiscoveryServiceTest() {
        super(200, TimeUnit.MILLISECONDS);
    }

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testNoDiscoveries() throws TimeoutException, InterruptedException {
        /* Given */
        final DefaultHiveDiscoveryService defaultHiveDiscoveryService = new DefaultHiveDiscoveryService(BRIDGE_UID, this.getTestingPhaser());
        defaultHiveDiscoveryService.addDiscoveryListener(this.discoveryListener);


        /* When */
        defaultHiveDiscoveryService.updateKnownNodes(Collections.emptySet());


        /* Then */
        this.awaitTestingPhaser();

        verify(this.discoveryListener, never()).thingDiscovered(any(), any());
    }
}
