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
package org.openhab.binding.mielecloud.internal.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.mielecloud.internal.config.MieleCloudConfigService;
import org.openhab.binding.mielecloud.internal.config.servlet.AccountOverviewServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.CreateBridgeServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.ForwardToLoginServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.ResultServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.SuccessServlet;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Common base class for all config flow tests.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractConfigFlowTest extends OpenHabOsgiTest {
    @Nullable
    private WebsiteCrawler crawler;

    @Nullable
    private AccountOverviewServlet accountOverviewServlet;

    @Nullable
    private ForwardToLoginServlet forwardToLoginServlet;

    @Nullable
    private ResultServlet resultServlet;

    @Nullable
    private SuccessServlet successServlet;

    @Nullable
    private CreateBridgeServlet createBridgeServlet;

    protected final WebsiteCrawler getCrawler() {
        final WebsiteCrawler crawler = this.crawler;
        assertNotNull(crawler);
        return Objects.requireNonNull(crawler);
    }

    protected final AccountOverviewServlet getAccountOverviewServlet() {
        final AccountOverviewServlet accountOverviewServlet = this.accountOverviewServlet;
        assertNotNull(accountOverviewServlet);
        return Objects.requireNonNull(accountOverviewServlet);
    }

    protected final ForwardToLoginServlet getForwardToLoginServlet() {
        final ForwardToLoginServlet forwardToLoginServlet = this.forwardToLoginServlet;
        assertNotNull(forwardToLoginServlet);
        return Objects.requireNonNull(forwardToLoginServlet);
    }

    protected final ResultServlet getResultServlet() {
        final ResultServlet resultServlet = this.resultServlet;
        assertNotNull(resultServlet);
        return Objects.requireNonNull(resultServlet);
    }

    protected final SuccessServlet getSuccessServlet() {
        final SuccessServlet successServlet = this.successServlet;
        assertNotNull(successServlet);
        return Objects.requireNonNull(successServlet);
    }

    protected final CreateBridgeServlet getCreateBridgeServlet() {
        final CreateBridgeServlet createBridgeServlet = this.createBridgeServlet;
        assertNotNull(createBridgeServlet);
        return Objects.requireNonNull(createBridgeServlet);
    }

    @BeforeEach
    public final void setUpConfigFlowTest() {
        setUpCrawler();
        setUpServlets();
    }

    private void setUpCrawler() {
        HttpClientFactory clientFactory = getService(HttpClientFactory.class);
        assertNotNull(clientFactory);
        crawler = new WebsiteCrawler(Objects.requireNonNull(clientFactory));
    }

    private void setUpServlets() {
        MieleCloudConfigService configService = getService(MieleCloudConfigService.class);
        assertNotNull(configService);

        accountOverviewServlet = configService.getAccountOverviewServlet();
        forwardToLoginServlet = configService.getForwardToLoginServlet();
        resultServlet = configService.getResultServlet();
        successServlet = configService.getSuccessServlet();
        createBridgeServlet = configService.getCreateBridgeServlet();
    }
}
