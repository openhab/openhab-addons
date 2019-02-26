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
package org.openhab.ui.classic.internal.render;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Video;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.component.annotations.Component;
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

        if (videoWidget.getEncoding() != null && videoWidget.getEncoding().toLowerCase().contains("mjpeg")) {
            // we handle mjpeg streams as an html image as browser can usually handle this
            snippet = getSnippet("image");
            snippet = StringUtils.replace(snippet, "%setrefresh%", "");
            snippet = StringUtils.replace(snippet, "%refresh%", "");
        } else {
            snippet = getSnippet("video");
        }
        String url = "../proxy?sitemap=" + sitemap + "&widgetId=" + widgetId;
        String mediaType = "";
        if (videoWidget.getEncoding() != null && videoWidget.getEncoding().toLowerCase().contains("hls")) {
            // For HTTP Live Stream we don't proxy the URL and we set the appropriate media type
            State state = itemUIRegistry.getState(w);
            url = (state instanceof StringType) ? state.toString() : videoWidget.getUrl();
            mediaType = "type=\"application/vnd.apple.mpegurl\"";
        }
        snippet = StringUtils.replace(snippet, "%url%", url);
        snippet = StringUtils.replace(snippet, "%media_type%", mediaType);
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
