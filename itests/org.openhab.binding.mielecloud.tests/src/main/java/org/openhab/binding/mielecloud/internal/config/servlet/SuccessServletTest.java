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
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.setPrivate;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.Website;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class SuccessServletTest extends AbstractConfigFlowTest {
    @Test
    public void whenTheSuccessPageIsShownThenAThingsFileTemplateIsPresent() throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Bridge mielecloud:account:genesis [ email=\"openhab@openhab.org\""));
    }

    @Test
    public void whenTheSuccessPageIsShownThenTheLocaleIsSelectedAutomatically() throws Exception {
        // given:
        LanguageProvider languageProvider = mock(LanguageProvider.class);
        when(languageProvider.getLanguage()).thenReturn(Optional.of("de"));
        setPrivate(getSuccessServlet(), "languageProvider", languageProvider);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("<option value=\"de\" selected=\"selected\">Deutsch - de</option>"));
        assertTrue(website.contains("locale=\"de\""));
    }

    @Test
    public void whenTheSuccessPageIsShownAndNoLocaleIsProvidedThenEnglishIsSelectedAutomatically() throws Exception {
        // given:
        LanguageProvider languageProvider = mock(LanguageProvider.class);
        when(languageProvider.getLanguage()).thenReturn(Optional.of("en"));
        setPrivate(getSuccessServlet(), "languageProvider", languageProvider);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("<option value=\"en\" selected=\"selected\">English - en</option>"));
        assertTrue(website.contains("locale=\"en\""));
    }

    @Test
    public void whenTheSuccessPageIsRequestedAndNoBridgeUidIsPassedThenTheFailurePageIsShown() throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing bridge UID."));
    }

    @Test
    public void whenTheSuccessPageIsRequestedAndAnEmptyBridgeUidIsPassedThenTheFailurePageIsShown() throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME
                + "=&" + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing bridge UID."));
    }

    @Test
    public void whenTheSuccessPageIsRequestedAndAMalformedBridgeUidIsPassedThenTheFailurePageIsShown()
            throws Exception {
        // when:
        Website website = getCrawler()
                .doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME + "=!genesis&"
                        + SuccessServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Malformed bridge UID."));
    }

    @Test
    public void whenTheSuccessPageIsRequestedAndNoEmailIsPassedThenTheFailurePageIsShown() throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString());

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing e-mail address."));
    }

    @Test
    public void whenTheSuccessPageIsRequestedAndAnEmptyEmailIsPassedThenTheFailurePageIsShown() throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/success?" + SuccessServlet.BRIDGE_UID_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID.getAsString() + "&"
                + SuccessServlet.EMAIL_PARAMETER_NAME + "=");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Missing e-mail address."));
    }
}
