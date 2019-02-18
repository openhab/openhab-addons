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
package org.openhab.ui.classic.internal.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.http.HttpContextFactoryService;
import org.eclipse.smarthome.model.sitemap.Chart;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.VisibilityRule;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.openhab.ui.classic.internal.WebAppConfig;
import org.openhab.ui.classic.internal.render.PageRenderer;
import org.openhab.ui.classic.internal.render.RenderException;
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
 * This is the main servlet for the Classic UI.
 * It serves the Html code based on the sitemap model.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(immediate = true, service = Servlet.class, configurationPid = "org.eclipse.smarthome.classicui", property = { //
        Constants.SERVICE_PID + "=org.eclipse.smarthome.classicui", //
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=ui", //
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=Classic UI", //
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=ui:classic" //
})
public class WebAppServlet extends BaseServlet {

    private final Logger logger = LoggerFactory.getLogger(WebAppServlet.class);

    private static final long serialVersionUID = 2154732940225805779L;

    /**
     * timeout for polling requests in milliseconds; if no state changes during this time,
     * an empty response is returned.
     */
    private static final long TIMEOUT_IN_MS = 30000L;

    /** the name of the servlet to be used in the URL */
    public static final String SERVLET_NAME = "app";

    private PageRenderer renderer;
    protected Set<SitemapProvider> sitemapProviders = new CopyOnWriteArraySet<>();

    private final WebAppConfig config = new WebAppConfig();

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC)
    protected void addSitemapProvider(SitemapProvider sitemapProvider) {
        this.sitemapProviders.add(sitemapProvider);
    }

    protected void removeSitemapProvider(SitemapProvider sitemapProvider) {
        this.sitemapProviders.remove(sitemapProvider);
    }

    @Reference
    protected void setPageRenderer(PageRenderer renderer) {
        renderer.setConfig(config);
        this.renderer = renderer;
    }

    protected void unsetPageRenderer(PageRenderer renderer) {
        this.renderer = null;
    }

    @Activate
    protected void activate(Map<String, Object> configProps, BundleContext bundleContext) {
        config.applyConfig(configProps);
        HttpContext httpContext = createHttpContext(bundleContext.getBundle());

        super.activate(WEBAPP_ALIAS + "/" + SERVLET_NAME, httpContext);
        try {
            httpService.registerResources(WEBAPP_ALIAS, "web", httpContext);
        } catch (NamespaceException e) {
            logger.error("Could not register static resources under {}", WEBAPP_ALIAS, e);
        }
    }

    @Modified
    protected void modified(Map<String, Object> configProps) {
        config.applyConfig(configProps);
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(WEBAPP_ALIAS + "/" + SERVLET_NAME);
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Classic UI");
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        logger.debug("Servlet request received!");

        // read request parameters
        String sitemapName = req.getParameter("sitemap");
        String widgetId = req.getParameter("w");
        boolean async = "true".equalsIgnoreCase(req.getParameter("__async"));
        boolean poll = "true".equalsIgnoreCase(req.getParameter("poll"));

        // if there are no parameters, display the "default" sitemap
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
                throw new RenderException("Sitemap '" + sitemapName + "' could not be found");
            }
            logger.debug("reading sitemap {} widgetId {} async {} poll {}", sitemap.getName(), widgetId, async, poll);
            if (widgetId == null || widgetId.isEmpty() || widgetId.equals("Home")) {
                // we are at the homepage, so we render the children of the sitemap root node
                String label = sitemap.getLabel() != null ? sitemap.getLabel() : sitemapName;
                EList<Widget> children = renderer.getItemUIRegistry().getChildren(sitemap);
                if (poll && !waitForChanges(children)) {
                    // we have reached the timeout, so we do not return any content as nothing has changed
                    res.getWriter().append(getTimeoutResponse()).close();
                    return;
                }
                result.append(renderer.processPage("Home", sitemapName, label, children, async));
            } else if (!widgetId.equals("Colorpicker")) {
                // we are on some subpage, so we have to render the children of the widget that has been selected
                Widget w = renderer.getItemUIRegistry().getWidget(sitemap, widgetId);
                if (w != null) {
                    if (!(w instanceof LinkableWidget)) {
                        throw new RenderException("Widget '" + w + "' can not have any content");
                    }
                    LinkableWidget lw = (LinkableWidget) w;
                    EList<Widget> children = renderer.getItemUIRegistry().getChildren(lw);
                    EList<Widget> parentAndChildren = new BasicEList<Widget>();
                    parentAndChildren.add(lw);
                    parentAndChildren.addAll(children);
                    if (poll && !waitForChanges(parentAndChildren)) {
                        // we have reached the timeout, so we do not return any content as nothing has changed
                        res.getWriter().append(getTimeoutResponse()).close();
                        return;
                    }
                    String label = renderer.getItemUIRegistry().getLabel(w);
                    if (label == null) {
                        label = "undefined";
                    }
                    result.append(renderer.processPage(renderer.getItemUIRegistry().getWidgetId(w), sitemapName, label,
                            children, async));
                }
            }
        } catch (RenderException e) {
            throw new ServletException(e.getMessage(), e);
        }
        if (async) {
            res.setContentType("application/xml;charset=UTF-8");
        } else {
            res.setContentType("text/html;charset=UTF-8");
        }
        res.getWriter().append(result);
        res.getWriter().close();
    }

    /**
     * Defines the response to return on a polling timeout.
     *
     * @return the response of the servlet on a polling timeout
     */
    private String getTimeoutResponse() {
        return "<root><part><destination mode=\"replace\" zone=\"timeout\" create=\"false\"/><data/></part></root>";
    }

    /**
     * This method only returns when a change has occurred to any item on the page to display
     *
     * @param widgets the widgets of the page to observe
     */
    private boolean waitForChanges(EList<Widget> widgets) {
        long startTime = (new Date()).getTime();
        boolean timeout = false;
        BlockingStateChangeListener listener = new BlockingStateChangeListener();
        // let's get all items for these widgets
        Set<GenericItem> items = getAllItems(widgets);
        for (GenericItem item : items) {
            item.addStateChangeListener(listener);
        }
        while (!listener.hasChangeOccurred() && !timeout) {
            timeout = (new Date()).getTime() - startTime > TIMEOUT_IN_MS;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                timeout = true;
                break;
            }
        }
        for (GenericItem item : items) {
            item.removeStateChangeListener(listener);
        }
        return !timeout;
    }

    /**
     * Collects all items that are represented by a given list of widgets
     *
     * @param widgets the widget list to get the items for
     * @return all items that are represented by the list of widgets
     */
    private Set<GenericItem> getAllItems(EList<Widget> widgets) {
        Set<GenericItem> items = new HashSet<GenericItem>();
        if (renderer.getItemUIRegistry() != null) {
            for (Widget widget : widgets) {
                // We skip the chart widgets having a refresh argument
                boolean skipWidget = false;
                if (widget instanceof Chart) {
                    Chart chartWidget = (Chart) widget;
                    skipWidget = chartWidget.getRefresh() > 0;
                }
                if (!skipWidget) {
                    addItemWithName(items, widget.getItem());
                }
                if (widget instanceof Frame) {
                    items.addAll(getAllItems(((Frame) widget).getChildren()));
                }
                for (VisibilityRule vr : widget.getVisibility()) {
                    addItemWithName(items, vr.getItem());
                }
            }
        }
        return items;
    }

    private void addItemWithName(Set<GenericItem> items, String itemName) {
        if (itemName != null) {
            try {
                Item item = renderer.getItemUIRegistry().getItem(itemName);
                if (item instanceof GenericItem) {
                    final GenericItem gItem = (GenericItem) item;
                    items.add(gItem);
                }
            } catch (ItemNotFoundException e) {
                // ignore
            }
        }
    }

    /**
     * This is a state change listener, which is merely used to determine, if a state
     * change has occurred on one of a list of items.
     *
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    private static class BlockingStateChangeListener implements StateChangeListener {

        private boolean changed = false;

        @Override
        public void stateChanged(Item item, State oldState, State newState) {
            if (!(item instanceof GroupItem)) {
                changed = true;
            }
        }

        /**
         * determines, whether a state change has occurred since its creation
         *
         * @return true, if a state has changed
         */
        public boolean hasChangeOccurred() {
            return changed;
        }

        @Override
        public void stateUpdated(Item item, State state) {
            if (item instanceof GroupItem) {
                changed = true;
            }
        }
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
