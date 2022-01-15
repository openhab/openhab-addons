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
package org.openhab.binding.mielecloud.internal.config;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.config.servlet.AccountOverviewServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.CreateBridgeServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.FailureServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.ForwardToLoginServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.PairAccountServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.ResourceLoader;
import org.openhab.binding.mielecloud.internal.config.servlet.ResultServlet;
import org.openhab.binding.mielecloud.internal.config.servlet.SuccessServlet;
import org.openhab.binding.mielecloud.internal.webservice.language.CombiningLanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.language.JvmLanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.language.OpenHabLanguageProvider;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the lifecycle of the Miele Cloud binding's configuration UI.
 *
 * @author Björn Lange - Initial Contribution
 */
@Component(service = MieleCloudConfigService.class, immediate = true, configurationPid = "binding.mielecloud.configService")
@NonNullByDefault
public final class MieleCloudConfigService {
    private static final String ROOT_ALIAS = "/mielecloud";
    private static final String PAIR_ALIAS = ROOT_ALIAS + "/pair";
    private static final String FORWARD_TO_LOGIN_ALIAS = ROOT_ALIAS + "/forwardToLogin";
    private static final String RESULT_ALIAS = ROOT_ALIAS + "/result";
    private static final String SUCCESS_ALIAS = ROOT_ALIAS + "/success";
    private static final String CREATE_BRIDGE_THING_ALIAS = ROOT_ALIAS + "/createBridgeThing";
    private static final String FAILURE_ALIAS = ROOT_ALIAS + "/failure";
    private static final String CSS_ALIAS = ROOT_ALIAS + "/assets/css";
    private static final String JS_ALIAS = ROOT_ALIAS + "/assets/js";
    private static final String IMG_ALIAS = ROOT_ALIAS + "/assets/img";

    private static final String WEBSITE_RESOURCE_BASE_PATH = "org/openhab/binding/mielecloud/internal/config";
    private static final String WEBSITE_CSS_RESOURCE_PATH = WEBSITE_RESOURCE_BASE_PATH + "/assets/css";
    private static final String WEBSITE_JS_RESOURCE_PATH = WEBSITE_RESOURCE_BASE_PATH + "/assets/js";
    private static final String WEBSITE_IMG_RESOURCE_PATH = WEBSITE_RESOURCE_BASE_PATH + "/assets/img";

    private final Logger logger = LoggerFactory.getLogger(MieleCloudConfigService.class);

    private HttpService httpService;
    private OAuthFactory oauthFactory;
    private Inbox inbox;
    private ThingRegistry thingRegistry;
    private LocaleProvider localeProvider;

    /**
     * For integration test purposes only.
     */
    @Nullable
    private AccountOverviewServlet accountOverviewServlet;

    /**
     * For integration test purposes only.
     */
    @Nullable
    private ForwardToLoginServlet forwardToLoginServlet;

    /**
     * For integration test purposes only.
     */
    @Nullable
    private ResultServlet resultServlet;

    /**
     * For integration test purposes only.
     */
    @Nullable
    private SuccessServlet successServlet;

    /**
     * For integration test purposes only.
     */
    @Nullable
    private CreateBridgeServlet createBridgeServlet;

    @Activate
    public MieleCloudConfigService(@Reference HttpService httpService, @Reference OAuthFactory oauthFactory,
            @Reference Inbox inbox, @Reference ThingRegistry thingRegistry, @Reference LocaleProvider localeProvider) {
        this.httpService = httpService;
        this.oauthFactory = oauthFactory;
        this.inbox = inbox;
        this.thingRegistry = thingRegistry;
        this.localeProvider = localeProvider;
    }

    @Nullable
    public AccountOverviewServlet getAccountOverviewServlet() {
        return accountOverviewServlet;
    }

    @Nullable
    public ForwardToLoginServlet getForwardToLoginServlet() {
        return forwardToLoginServlet;
    }

    @Nullable
    public ResultServlet getResultServlet() {
        return resultServlet;
    }

    @Nullable
    public SuccessServlet getSuccessServlet() {
        return successServlet;
    }

    @Nullable
    public CreateBridgeServlet getCreateBridgeServlet() {
        return createBridgeServlet;
    }

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        registerWebsite(componentContext.getBundleContext());
    }

    private void registerWebsite(BundleContext bundleContext) {
        ResourceLoader resourceLoader = new ResourceLoader(WEBSITE_RESOURCE_BASE_PATH, bundleContext);
        OAuthAuthorizationHandler authorizationHandler = new OAuthAuthorizationHandlerImpl(oauthFactory,
                ThreadPoolManager.getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON));

        try {
            HttpContext httpContext = httpService.createDefaultHttpContext();
            httpService.registerServlet(ROOT_ALIAS,
                    accountOverviewServlet = new AccountOverviewServlet(resourceLoader, thingRegistry, inbox),
                    new Hashtable<>(), httpContext);
            httpService.registerServlet(PAIR_ALIAS, new PairAccountServlet(resourceLoader), new Hashtable<>(),
                    httpContext);
            httpService.registerServlet(FORWARD_TO_LOGIN_ALIAS,
                    forwardToLoginServlet = new ForwardToLoginServlet(authorizationHandler), new Hashtable<>(),
                    httpContext);
            httpService.registerServlet(RESULT_ALIAS, resultServlet = new ResultServlet(authorizationHandler),
                    new Hashtable<>(), httpContext);
            httpService.registerServlet(SUCCESS_ALIAS,
                    successServlet = new SuccessServlet(resourceLoader, createLanguageProvider()), new Hashtable<>(),
                    httpContext);
            httpService.registerServlet(CREATE_BRIDGE_THING_ALIAS,
                    createBridgeServlet = new CreateBridgeServlet(inbox, thingRegistry), new Hashtable<>(),
                    httpContext);
            httpService.registerServlet(FAILURE_ALIAS, new FailureServlet(resourceLoader), new Hashtable<>(),
                    httpContext);
            httpService.registerResources(CSS_ALIAS, WEBSITE_CSS_RESOURCE_PATH, httpContext);
            httpService.registerResources(JS_ALIAS, WEBSITE_JS_RESOURCE_PATH, httpContext);
            httpService.registerResources(IMG_ALIAS, WEBSITE_IMG_RESOURCE_PATH, httpContext);
            logger.debug("Registered Miele Cloud binding website at /mielecloud");
        } catch (NamespaceException | ServletException e) {
            logger.warn(
                    "Failed to register Miele Cloud binding website. Miele Cloud binding website will not be available.",
                    e);
            unregisterWebsite();
        }
    }

    private LanguageProvider createLanguageProvider() {
        return new CombiningLanguageProvider(new OpenHabLanguageProvider(localeProvider), new JvmLanguageProvider());
    }

    @Deactivate
    protected void deactivate() {
        unregisterWebsite();
    }

    private void unregisterWebsite() {
        unregisterWebResource(ROOT_ALIAS);
        unregisterWebResource(PAIR_ALIAS);
        unregisterWebResource(FORWARD_TO_LOGIN_ALIAS);
        unregisterWebResource(RESULT_ALIAS);
        unregisterWebResource(SUCCESS_ALIAS);
        unregisterWebResource(CREATE_BRIDGE_THING_ALIAS);
        unregisterWebResource(CSS_ALIAS);
        unregisterWebResource(JS_ALIAS);
        unregisterWebResource(IMG_ALIAS);
        forwardToLoginServlet = null;
        resultServlet = null;
        createBridgeServlet = null;
        logger.debug("Unregistered Miele Cloud binding website at /mielecloud");
    }

    private void unregisterWebResource(String alias) {
        try {
            httpService.unregister(alias);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to unregister Miele Cloud binding website alias {}", alias, e);
        }
    }
}
