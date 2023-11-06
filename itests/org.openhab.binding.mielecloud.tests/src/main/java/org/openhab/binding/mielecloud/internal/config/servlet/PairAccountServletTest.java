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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.Website;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class PairAccountServletTest extends AbstractConfigFlowTest {
    private static final String CLIENT_ID_INPUT_NAME = "clientId";
    private static final String CLIENT_SECRET_INPUT_NAME = "clientSecret";

    @Test
    public void whenPairAccountIsInvokedWithClientIdParameterThenTheParameterIsPlacedInTheInputBox() throws Exception {
        // when:
        Website pairAccountSite = getCrawler()
                .doGetRelative("/mielecloud/pair?" + PairAccountServlet.CLIENT_ID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.CLIENT_ID);

        // then:
        assertEquals(MieleCloudBindingIntegrationTestConstants.CLIENT_ID,
                pairAccountSite.getValueOfInput(CLIENT_ID_INPUT_NAME));
        assertEquals("", pairAccountSite.getValueOfInput(CLIENT_SECRET_INPUT_NAME));
    }

    @Test
    public void whenPairAccountIsInvokedWithClientSecretParameterThenTheParameterIsPlacedInTheInputBox()
            throws Exception {
        // when:
        Website pairAccountSite = getCrawler()
                .doGetRelative("/mielecloud/pair?" + PairAccountServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET);

        // then:
        assertEquals("", pairAccountSite.getValueOfInput(CLIENT_ID_INPUT_NAME));
        assertEquals(MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET,
                pairAccountSite.getValueOfInput(CLIENT_SECRET_INPUT_NAME));
    }
}
