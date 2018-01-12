/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.servlet;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.openhab.ui.cometvisu.internal.Config;
import org.openhab.ui.cometvisu.internal.util.ClientInstaller;
import org.openhab.ui.cometvisu.php.PHProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * registers the CometVisuServlet-Service
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class CometVisuApp {

    private final Logger logger = LoggerFactory.getLogger(CometVisuApp.class);

    protected HttpService httpService;

    private ItemUIRegistry itemUIRegistry;

    private ItemRegistry itemRegistry;

    private Set<SitemapProvider> sitemapProviders = new HashSet<>();

    private List<IconProvider> iconProviders = new ArrayList<>();

    private EventPublisher eventPublisher;

    private CometVisuServlet servlet;

    private PHProvider phpProvider;

    protected static Map<String, QueryablePersistenceService> persistenceServices = new HashMap<>();

    private final ClientInstaller installer = ClientInstaller.getInstance();

    private Map<String, Object> properties = new HashMap<>();

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> props) {
        properties = props;
    }

    public CometVisuServlet getServlet() {
        return servlet;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    public EventPublisher getEventPublisher() {
        return this.eventPublisher;
    }

    public void addPersistenceService(PersistenceService service) {
        if (service instanceof QueryablePersistenceService) {
            persistenceServices.put(service.getId(), (QueryablePersistenceService) service);
        }
    }

    public void removePersistenceService(PersistenceService service) {
        persistenceServices.remove(service.getId());
    }

    public static Map<String, QueryablePersistenceService> getPersistenceServices() {
        return persistenceServices;
    }

    public List<IconProvider> getIconProviders() {
        return iconProviders;
    }

    public void addIconProvider(IconProvider iconProvider) {
        this.iconProviders.add(iconProvider);
    }

    public void removeIconProvider(IconProvider iconProvider) {
        this.iconProviders.remove(iconProvider);
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    public void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
    }

    public void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    public void addSitemapProvider(SitemapProvider provider) {
        sitemapProviders.add(provider);
    }

    public void removeSitemapProvider(SitemapProvider provider) {
        sitemapProviders.remove(provider);
    }

    public ItemUIRegistry getItemUIRegistry() {
        return itemUIRegistry;
    }

    public Set<SitemapProvider> getSitemapProviders() {
        return sitemapProviders;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    public void setPHProvider(PHProvider prov) {
        this.phpProvider = prov;
        if (servlet != null) {
            servlet.setPHProvider(prov);
        }
    }

    public PHProvider getPHProvider() {
        return this.phpProvider;
    }

    public void unsetPHProvider() {
        this.phpProvider = null;
        if (servlet != null) {
            servlet.unsetPHProvider();
        }
    }

    private void readConfiguration(final Map<String, Object> properties) {
        if (properties != null) {
            setProperties(properties);
            if (properties.get(Config.COMETVISU_WEBFOLDER_PROPERTY) != null) {
                Config.COMETVISU_WEBFOLDER = (String) properties.get(Config.COMETVISU_WEBFOLDER_PROPERTY);
            }
            if (properties.get(Config.COMETVISU_WEBAPP_ALIAS_PROPERTY) != null) {
                Config.COMETVISU_WEBAPP_ALIAS = (String) properties.get(Config.COMETVISU_WEBAPP_ALIAS_PROPERTY);
            }
            if (properties.get(Config.COMETVISU_AUTODOWNLOAD_PROPERTY) != null) {
                Boolean newValue = (Boolean) properties.get(Config.COMETVISU_AUTODOWNLOAD_PROPERTY);
                boolean changed = Config.COMETVISU_AUTO_DOWNLOAD != newValue;
                Config.COMETVISU_AUTO_DOWNLOAD = newValue;
                if (Config.COMETVISU_AUTO_DOWNLOAD && changed) {
                    // let the installer check if the CometVisu client is installed and do that if not
                    installer.check();
                }
                Config.COMETVISU_AUTO_DOWNLOAD = newValue;
            }
            for (String key : properties.keySet()) {
                String[] parts = key.split(">");
                String propKey = parts.length > 1 ? parts[1] : parts[0];
                String propPid = parts.length > 1 ? parts[0] : "";

                logger.debug("Property: {}->{}:{}, Parts {}", propPid, propKey, properties.get(key), parts.length);
                if (!propPid.isEmpty()) {
                    if (Config.configMappings.containsKey(propPid)) {
                        Config.configMappings.get(propPid).put(propKey, properties.get(key));
                    }
                }
            }
        }
    }

    /**
     * Called by the SCR to activate the component with its configuration read
     * from CAS
     *
     * @param bundleContext
     *            BundleContext of the Bundle that defines this component
     * @param configuration
     *            Configuration properties for this component obtained from the
     *            ConfigAdmin service
     */
    protected void activate(Map<String, Object> configProps) throws ConfigurationException {
        readConfiguration(configProps);
        registerServlet();
        logger.info("Started CometVisu UI at {} serving {}", Config.COMETVISU_WEBAPP_ALIAS, Config.COMETVISU_WEBFOLDER);
    }

    public void deactivate(BundleContext componentContext) {
        unregisterServlet();
        logger.info("Stopped CometVisu UI");
    }

    private void registerServlet() {
        // As the alias is user configurable, we have to check if it has a
        // trailing slash but no leading slash
        if (!Config.COMETVISU_WEBAPP_ALIAS.startsWith("/")) {
            Config.COMETVISU_WEBAPP_ALIAS = "/" + Config.COMETVISU_WEBAPP_ALIAS;
        }

        if (Config.COMETVISU_WEBAPP_ALIAS.endsWith("/")) {
            Config.COMETVISU_WEBAPP_ALIAS = Config.COMETVISU_WEBAPP_ALIAS.substring(0,
                    Config.COMETVISU_WEBAPP_ALIAS.length() - 1);
        }

        Dictionary<String, String> servletParams = new Hashtable<String, String>();
        servlet = new CometVisuServlet(Config.COMETVISU_WEBFOLDER, this);
        try {
            httpService.registerServlet(Config.COMETVISU_WEBAPP_ALIAS, servlet, servletParams, null);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    private void unregisterServlet() {
        httpService.unregister(Config.COMETVISU_WEBAPP_ALIAS);
    }

    /**
     * Called by the SCR when the configuration of a binding has been changed
     * through the ConfigAdmin service.
     *
     * @param configuration
     *            Updated configuration properties
     */
    protected void modified(Map<String, Object> configProps) throws ConfigurationException {
        logger.info("updated({})", configProps);
        if (configProps == null) {
            return;
        }
        if (configProps.containsKey(Config.COMETVISU_WEBFOLDER_PROPERTY)
                || configProps.containsKey(Config.COMETVISU_WEBAPP_ALIAS_PROPERTY)) {
            unregisterServlet();
        }
        readConfiguration(configProps);
        if (configProps.containsKey(Config.COMETVISU_WEBFOLDER_PROPERTY)
                || configProps.containsKey(Config.COMETVISU_WEBAPP_ALIAS_PROPERTY)) {
            registerServlet();
        }
    }

}
