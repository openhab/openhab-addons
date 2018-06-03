/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.openhab.ui.dashboard.DashboardTile;

/**
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class CometVisuDashboardTile implements DashboardTile {
    private Set<SitemapProvider> sitemapProviders = new HashSet<>();

    public void addSitemapProvider(SitemapProvider provider) {
        sitemapProviders.add(provider);
    }

    public void removeSitemapProvider(SitemapProvider provider) {
        sitemapProviders.remove(provider);
    }

    @Override
    public String getName() {
        return "CometVisu";
    }

    @Override
    public String getUrl() {

        Set<String> sitemapNames = new HashSet<>();
        // collect all sitemap names
        for (SitemapProvider provider : sitemapProviders) {
            sitemapNames.addAll(provider.getSitemapNames());
        }
        String sitemap = "demo";
        if (sitemapNames.size() > 0) {
            if (sitemapNames.contains("demo")) {
                // a demo sitemap exists, use this one instead
                sitemap = "oh_demo";
            } else {
                // use the first available
                sitemap = "oh_" + sitemapNames.iterator().next();
            }
        }
        String path = "/";
        // lets find the index.html
        File root = new File(Config.COMETVISU_WEBFOLDER);
        File index = new File(root, "index.html");
        if (index.exists()) {
            path = "/index.html";
        } else {
            File build = new File(root, "build");
            if ((new File(build, "index.html")).exists()) {
                path = "/build/index.html";
            } else {
                File source = new File(root, "source");
                if ((new File(source, "index.html")).exists()) {
                    path = "/source/index.html";
                } else {
                    File src = new File(root, "src");
                    if ((new File(src, "index.html")).exists()) {
                        path = "/src/index.html";
                    }
                }
            }
        }
        return Config.COMETVISU_WEBAPP_ALIAS + path + "?config=" + sitemap;
    }

    @Override
    public String getOverlay() {
        return "html5";
    }

    @Override
    public String getImageUrl() {
        return "img/cometvisu.png";
    }
}
