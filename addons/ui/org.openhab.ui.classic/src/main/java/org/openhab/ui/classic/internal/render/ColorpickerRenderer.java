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
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Colorpicker;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.openhab.ui.classic.internal.servlet.WebAppServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * <p>
 * This is an implementation of the {@link WidgetRenderer} interface, which can produce HTML code for Colorpicker
 * widgets.
 *
 * <p>
 * Note: This renderer requires the files "jquery.miniColors.css" and "jquery.miniColors.js" in the web folder of this
 * bundle
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(service = WidgetRenderer.class)
public class ColorpickerRenderer extends AbstractWidgetRenderer {

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Colorpicker;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Colorpicker cp = (Colorpicker) w;

        String snippetName = "colorpicker";

        String snippet = getSnippet(snippetName);

        // set the default send-update frequency to 200ms
        String frequency = cp.getFrequency() == 0 ? "200" : Integer.toString(cp.getFrequency());

        // get RGB hex value
        State state = itemUIRegistry.getState(cp);
        String hexValue = "#ffffff";
        if (state instanceof HSBType) {
            HSBType hsbState = (HSBType) state;
            hexValue = "#" + Integer.toHexString(hsbState.getRGB()).substring(2);
        }
        String label = getLabel(cp);
        String purelabel = itemUIRegistry.getLabel(w);
        purelabel = purelabel.replaceAll("\\\"", "\\\\'");

        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(cp));
        snippet = StringUtils.replace(snippet, "%category%", getCategory(cp));
        snippet = StringUtils.replace(snippet, "%item%", w.getItem());
        snippet = StringUtils.replace(snippet, "%label%", label);
        snippet = StringUtils.replace(snippet, "%format%", getFormat());
        snippet = StringUtils.replace(snippet, "%purelabel%", purelabel);
        snippet = StringUtils.replace(snippet, "%state%", hexValue);
        snippet = StringUtils.replace(snippet, "%frequency%", frequency);
        snippet = StringUtils.replace(snippet, "%servletname%", WebAppServlet.SERVLET_NAME);

        String style = "";
        String color = itemUIRegistry.getLabelColor(w);
        if (color != null) {
            style = "color:" + color;
        }
        snippet = StringUtils.replace(snippet, "%labelstyle%", style);

        style = "";
        color = itemUIRegistry.getValueColor(w);
        if (color != null) {
            style = "color:" + color;
        }
        snippet = StringUtils.replace(snippet, "%valuestyle%", style);

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
