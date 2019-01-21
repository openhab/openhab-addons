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
package org.openhab.ui.basic.internal.render;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.openhab.ui.basic.render.RenderException;
import org.openhab.ui.basic.render.WidgetRenderer;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Image widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
@Component(service = WidgetRenderer.class)
public class ImageRenderer extends AbstractWidgetRenderer {

    private static final String URL_NONE_ICON = "images/none.png";

    @Override
    @Activate
    protected void activate(BundleContext bundleContext) {
        super.activate(bundleContext);
    }

    @Override
    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        super.deactivate(bundleContext);
    }

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Image;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Image image = (Image) w;
        String snippet = (image.getChildren().size() > 0) ? getSnippet("image_link") : getSnippet("image");

        if (image.getRefresh() > 0) {
            snippet = StringUtils.replace(snippet, "%update_interval%", Integer.toString(image.getRefresh()));
        } else {
            snippet = StringUtils.replace(snippet, "%update_interval%", "0");
        }

        String widgetId = itemUIRegistry.getWidgetId(w);
        snippet = StringUtils.replace(snippet, "%id%", widgetId);
        snippet = preprocessSnippet(snippet, w);

        String sitemap = null;
        if (w.eResource() != null) {
            sitemap = w.eResource().getURI().path();
        }
        boolean validUrl = isValidURL(image.getUrl());
        String proxiedUrl = "../proxy?sitemap=" + sitemap + "&amp;widgetId=" + widgetId;
        State state = itemUIRegistry.getState(w);
        String url;
        boolean ignoreRefresh;
        if (!itemUIRegistry.getVisiblity(w)) {
            url = URL_NONE_ICON;
            ignoreRefresh = true;
        } else if (state instanceof RawType) {
            url = state.toFullString();
            ignoreRefresh = true;
        } else if ((sitemap != null) && ((state instanceof StringType) || validUrl)) {
            url = proxiedUrl + "&amp;t=" + (new Date()).getTime();
            ignoreRefresh = false;
        } else {
            url = URL_NONE_ICON;
            ignoreRefresh = true;
        }
        snippet = StringUtils.replace(snippet, "%valid_url%", validUrl ? "true" : "false");
        snippet = StringUtils.replace(snippet, "%proxied_url%", proxiedUrl);
        snippet = StringUtils.replace(snippet, "%ignore_refresh%", ignoreRefresh ? "true" : "false");
        snippet = StringUtils.replace(snippet, "%url%", url);

        sb.append(snippet);
        return null;
    }

    @Override
    @Reference
    protected void setItemUIRegistry(ItemUIRegistry ItemUIRegistry) {
        super.setItemUIRegistry(ItemUIRegistry);
    }

    @Override
    protected void unsetItemUIRegistry(ItemUIRegistry ItemUIRegistry) {
        super.unsetItemUIRegistry(ItemUIRegistry);
    }

}
