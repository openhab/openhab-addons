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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Video;
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
 * can produce HTML code for Video widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(service = WidgetRenderer.class)
public class VideoRenderer extends AbstractWidgetRenderer {

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
        return w instanceof Video;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Video videoWidget = (Video) w;
        String snippet = null;

        String widgetId = itemUIRegistry.getWidgetId(w);
        String sitemap = w.eResource().getURI().path();

        // we handle mjpeg streams as an html image as browser can usually handle this
        String snippetName = (videoWidget.getEncoding() != null
                && videoWidget.getEncoding().toLowerCase().contains("mjpeg")) ? "image" : "video";

        snippet = getSnippet(snippetName);
        snippet = preprocessSnippet(snippet, w);

        State state = itemUIRegistry.getState(w);
        String url;
        if (snippetName.equals("image")) {
            boolean validUrl = isValidURL(videoWidget.getUrl());
            String proxiedUrl = "../proxy?sitemap=" + sitemap + "&amp;widgetId=" + widgetId;
            if (!itemUIRegistry.getVisiblity(w)) {
                url = URL_NONE_ICON;
            } else if ((sitemap != null) && ((state instanceof StringType) || validUrl)) {
                url = proxiedUrl + "&amp;t=" + (new Date()).getTime();
            } else {
                url = URL_NONE_ICON;
            }
            snippet = StringUtils.replace(snippet, "%valid_url%", validUrl ? "true" : "false");
            snippet = StringUtils.replace(snippet, "%proxied_url%", proxiedUrl);
            snippet = StringUtils.replace(snippet, "%update_interval%", "0");
            snippet = StringUtils.replace(snippet, "%ignore_refresh%", "true");
            snippet = StringUtils.replace(snippet, "%url%", url);
        } else {
            String mediaType;
            if (videoWidget.getEncoding() != null && videoWidget.getEncoding().toLowerCase().contains("hls")) {
                // For HTTP Live Stream we don't proxy the URL and we set the appropriate media type
                url = (state instanceof StringType) ? state.toString() : videoWidget.getUrl();
                mediaType = "type=\"application/vnd.apple.mpegurl\"";
            } else {
                url = "../proxy?sitemap=" + sitemap + "&widgetId=" + widgetId;
                mediaType = "";
            }
            snippet = StringUtils.replace(snippet, "%url%", url);
            snippet = StringUtils.replace(snippet, "%media_type%", mediaType);
        }

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
