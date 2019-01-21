/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.ui.basic.internal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.io.http.HttpContextFactoryService;
import org.eclipse.smarthome.io.rest.sitemap.SitemapSubscriptionService;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.openhab.ui.basic.internal.WebAppConfig;
import org.openhab.ui.basic.internal.render.PageRenderer;
import org.openhab.ui.basic.render.RenderException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main servlet for the Basic UI.
 * It serves the Html code based on the sitemap model.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - Basic UI changes
 *
 */
@Component(immediate = true, service = Servlet.class, configurationPid = "org.openhab.basicui", property = { //
        Constants.SERVICE_PID + "=org.openhab.basicui", //
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=ui:basic", //
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=ui", //
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=Basic UI" //
})
public class WebAppServlet extends BaseServlet {

    private final Logger logger = LoggerFactory.getLogger(WebAppServlet.class);

    private static final long serialVersionUID = 3443749654545136365L;

    /** the name of the servlet to be used in the URL */
    public static final String SERVLET_NAME = "app";

    private static final String CONTENT_TYPE_ASYNC = "application/xml;charset=UTF-8";
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private PageRenderer renderer;
    private SitemapSubscriptionService subscriptions;
    private final WebAppConfig config = new WebAppConfig();
    protected Set<SitemapProvider> sitemapProviders = new CopyOnWriteArraySet<>();

    @Reference
    public void setSitemapSubscriptionService(SitemapSubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void unsetSitemapSubscriptionService(SitemapSubscriptionService subscriptions) {
        this.subscriptions = null;
    }

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC)
    public void addSitemapProvider(SitemapProvider sitemapProvider) {
        this.sitemapProviders.add(sitemapProvider);
    }

    public void removeSitemapProvider(SitemapProvider sitemapProvider) {
        this.sitemapProviders.remove(sitemapProvider);
    }

    @Reference
    public void setPageRenderer(PageRenderer renderer) {
        renderer.setConfig(config);
        this.renderer = renderer;
    }

    public void unsetPageRenderer(PageRenderer renderer) {
        this.renderer = null;
    }

    @Activate
    protected void activate(Map<String, Object> configProps, BundleContext bundleContext) {
        HttpContext httpContext = createHttpContext(bundleContext.getBundle());
        super.activate(WEBAPP_ALIAS + "/" + SERVLET_NAME, httpContext);

        try {
            httpService.registerResources(WEBAPP_ALIAS, "web", httpContext);
        } catch (NamespaceException e) {
            logger.error("Could not register static resources under {}", WEBAPP_ALIAS, e);
        }

        config.applyConfig(configProps);
    }

    @Modified
    protected void modified(Map<String, Object> configProps) {
        config.applyConfig(configProps);
    }

    @Deactivate
    protected void deactivate() {
        super.deactivate(WEBAPP_ALIAS + "/" + SERVLET_NAME);
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Basic UI");
    }

    private void showSitemapList(ServletResponse res) throws IOException, RenderException {
        PrintWriter resWriter;
        resWriter = res.getWriter();
        resWriter.append(renderer.renderSitemapList(sitemapProviders));

        res.setContentType(CONTENT_TYPE);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.debug("Servlet request received!");

        // read request parameters
        String sitemapName = req.getParameter("sitemap");
        String widgetId = req.getParameter("w");
        String subscriptionId = req.getParameter("subscriptionId");
        boolean async = "true".equalsIgnoreCase(req.getParameter("__async"));

        if (sitemapName == null) {
            sitemapName = config.getDefaultSitemap();
        }

        StringBuilder result = new StringBuilder();
        Sitemap sitemap = null;

        for (SitemapProvider sitemapProvider : sitemapProviders) {
            sitemap = sitemapProvider.getSitemap(sitemapName);
            if (sitemap != null) {
                break;
            }
        }

        try {
            if (sitemap == null) {
                showSitemapList(res);
                return;
            }

            logger.debug("reading sitemap {}", sitemap.getName());
            if (widgetId == null || widgetId.isEmpty() || widgetId.equals(sitemapName)) {
                // we are at the homepage, so we render the children of the sitemap root node
                if (subscriptionId != null) {
                    if (subscriptions.exists(subscriptionId)) {
                        subscriptions.setPageId(subscriptionId, sitemap.getName(), sitemapName);
                    } else {
                        logger.debug("Basic UI requested a non-existing event subscription id ({})", subscriptionId);
                    }
                }
                String label = sitemap.getLabel() != null ? sitemap.getLabel() : sitemapName;
                EList<Widget> children = renderer.getItemUIRegistry().getChildren(sitemap);
                result.append(renderer.processPage(sitemapName, sitemapName, label, children, async));
            } else if (!widgetId.equals("Colorpicker")) {
                // we are on some subpage, so we have to render the children of the widget that has been selected
                if (subscriptionId != null) {
                    if (subscriptions.exists(subscriptionId)) {
                        subscriptions.setPageId(subscriptionId, sitemap.getName(), widgetId);
                    } else {
                        logger.debug("Basic UI requested a non-existing event subscription id ({})", subscriptionId);
                    }
                }
                Widget w = renderer.getItemUIRegistry().getWidget(sitemap, widgetId);
                if (w != null) {
                    String label = renderer.getItemUIRegistry().getLabel(w);
                    if (label == null) {
                        label = "undefined";
                    }
                    if (!(w instanceof LinkableWidget)) {
                        throw new RenderException("Widget '" + w + "' can not have any content");
                    }
                    EList<Widget> children = renderer.getItemUIRegistry().getChildren((LinkableWidget) w);
                    result.append(renderer.processPage(renderer.getItemUIRegistry().getWidgetId(w), sitemapName, label,
                            children, async));
                }
            }
        } catch (RenderException e) {
            throw new ServletException(e.getMessage(), e);
        }
        if (async) {
            res.setContentType(CONTENT_TYPE_ASYNC);
        } else {
            res.setContentType(CONTENT_TYPE);
        }
        res.getWriter().append(result);
        res.getWriter().close();
    }

    @Override
    @Reference
    public void setItemRegistry(ItemRegistry ItemRegistry) {
        super.setItemRegistry(ItemRegistry);
    }

    @Override
    public void unsetItemRegistry(ItemRegistry ItemRegistry) {
        super.unsetItemRegistry(ItemRegistry);
    }

    @Override
    @Reference
    public void setHttpService(HttpService HttpService) {
        super.setHttpService(HttpService);
    }

    @Override
    public void unsetHttpService(HttpService HttpService) {
        super.unsetHttpService(HttpService);
    }

    @Override
    @Reference
    public void setHttpContextFactoryService(HttpContextFactoryService HttpContextFactoryService) {
        super.setHttpContextFactoryService(HttpContextFactoryService);
    }

    @Override
    public void unsetHttpContextFactoryService(HttpContextFactoryService HttpContextFactoryService) {
        super.unsetHttpContextFactoryService(HttpContextFactoryService);
    }

}
