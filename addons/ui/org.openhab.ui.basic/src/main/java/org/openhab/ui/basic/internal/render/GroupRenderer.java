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

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.model.sitemap.Group;
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
 * can produce HTML code for Group widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
@Component(service = WidgetRenderer.class)
public class GroupRenderer extends AbstractWidgetRenderer {

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
        return w instanceof Group;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        String snippet = getSnippet("group");

        snippet = preprocessSnippet(snippet, w);
        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(w));

        // Process the color tags
        snippet = processColor(w, snippet);

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
