/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.config.servlet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.ReflectionUtil;
import org.openhab.binding.mielecloud.internal.util.Website;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class AccountOverviewServletTest extends AbstractConfigFlowTest {
    @Test
    public void whenAccountOverviewServletIsCalledOverNonSslConnectionThenAWarningIsShown() throws Exception {
        // when:
        Website accountOverviewSite = getCrawler().doGetRelative("/mielecloud");

        // then:
        assertTrue(accountOverviewSite
                .contains("Warning: We strongly advice to proceed only with SSL enabled for a secure data exchange."));
        assertTrue(accountOverviewSite.contains(
                "See <a href=\"https://www.openhab.org/docs/installation/security.html\">Securing access to openHAB</a> for details."));
    }

    @Test
    public void whenAccountOverviewServletIsCalledAndNoBridgeIsPresentThenThePageSaysThatThereIsNoBridgePaired()
            throws Exception {
        // when:
        Website accountOverviewSite = getCrawler().doGetRelative("/mielecloud");

        // then:
        assertTrue(accountOverviewSite.contains("There is no account paired at the moment."));
    }

    @Test
    public void whenAccountOverviewServletIsCalledAndBridgesArePresentThenThePageDisplaysInformationAboutThem()
            throws Exception {
        // given:
        Configuration configuration1 = mock(Configuration.class);
        when(configuration1.get(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE)).thenReturn("de");
        when(configuration1.get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL)).thenReturn("openhab@openhab.org");

        Bridge bridge1 = mock(Bridge.class);
        when(bridge1.getThingTypeUID()).thenReturn(MieleCloudBindingConstants.THING_TYPE_BRIDGE);
        when(bridge1.getUID()).thenReturn(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID);
        when(bridge1.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge1.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(bridge1.getConfiguration()).thenReturn(configuration1);

        Configuration configuration2 = mock(Configuration.class);
        when(configuration2.get(MieleCloudBindingConstants.CONFIG_PARAM_LOCALE)).thenReturn("en");
        when(configuration2.get(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL)).thenReturn("everyone@openhab.org");

        Bridge bridge2 = mock(Bridge.class);
        when(bridge2.getThingTypeUID()).thenReturn(MieleCloudBindingConstants.THING_TYPE_BRIDGE);
        when(bridge2.getUID()).thenReturn(new ThingUID(MieleCloudBindingConstants.THING_TYPE_BRIDGE, "test"));
        when(bridge2.getStatus()).thenReturn(ThingStatus.OFFLINE);
        when(bridge2.getStatusInfo()).thenReturn(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error message"));
        when(bridge2.getConfiguration()).thenReturn(configuration2);

        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        when(thingRegistry.stream()).thenAnswer(invocation -> Stream.of(bridge1, bridge2));
        ReflectionUtil.setPrivate(getAccountOverviewServlet(), "thingRegistry", thingRegistry);

        // when:
        Website accountOverviewSite = getCrawler().doGetRelative("/mielecloud");

        // then:
        assertTrue(accountOverviewSite.contains("The following bridges are paired"));
        assertTrue(accountOverviewSite.contains("openhab@openhab.org"));
        assertTrue(accountOverviewSite.contains("mielecloud:account:genesis"));
        assertTrue(accountOverviewSite.contains("<span class=\"status online\">"));
        assertTrue(accountOverviewSite.contains("everyone@openhab.org"));
        assertTrue(accountOverviewSite.contains("mielecloud:account:test"));
        assertTrue(accountOverviewSite.contains("<span class=\"status offline\">"));
    }
}
