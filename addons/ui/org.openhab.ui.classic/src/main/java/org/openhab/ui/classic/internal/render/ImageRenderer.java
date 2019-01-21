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

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Image widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(service = WidgetRenderer.class)
public class ImageRenderer extends AbstractWidgetRenderer {

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Image;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Image image = (Image) w;
        String snippet = (image.getChildren().size() > 0) ? getSnippet("image_link") : getSnippet("image");

        if (image.getRefresh() > 0) {
            snippet = StringUtils.replace(snippet, "%refresh%", "id=\"%id%\" data-timeout=\"" + image.getRefresh()
                    + "\" onload=\"startReloadImage('%url%', '%id%')\"");
        } else {
            snippet = StringUtils.replace(snippet, "%refresh%", "");
        }

        String widgetId = itemUIRegistry.getWidgetId(w);
        snippet = StringUtils.replace(snippet, "%id%", widgetId);

        String sitemap = null;
        if (w.eResource() != null) {
            sitemap = w.eResource().getURI().path();
        }
        boolean validUrl = isValidURL(image.getUrl());
        String proxiedUrl = "../proxy?sitemap=" + sitemap + "&widgetId=" + widgetId;
        State state = itemUIRegistry.getState(w);
        String url;
        if (state instanceof RawType) {
            url = state.toFullString();
        } else if ((sitemap != null) && ((state instanceof StringType) || validUrl)) {
            url = proxiedUrl + "&t=" + (new Date()).getTime();
        } else {
            url = "images/none.png";
        }
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
