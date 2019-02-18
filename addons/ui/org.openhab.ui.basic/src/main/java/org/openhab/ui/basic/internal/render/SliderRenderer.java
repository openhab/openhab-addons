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
import org.eclipse.smarthome.model.sitemap.Slider;
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
 * <p>
 * This is an implementation of the {@link WidgetRenderer} interface, which can produce HTML code for Slider widgets.
 *
 * <p>
 * Note: As the WebApp.Net framework cannot render real sliders in the UI, we instead show buttons to increase or
 * decrease the value.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 * @author Florian Schmidt - Make min and max value configurable in Sitemap
 *
 */
@Component(service = WidgetRenderer.class)
public class SliderRenderer extends AbstractWidgetRenderer {

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
        return w instanceof Slider;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Slider s = (Slider) w;

        String snippetName = "slider";
        String snippet = getSnippet(snippetName);

        // set the default send-update frequency to 200ms
        String frequency = s.getFrequency() == 0 ? "200" : Integer.toString(s.getFrequency());

        String unit = getUnitForWidget(w);

        snippet = preprocessSnippet(snippet, w);
        snippet = StringUtils.replace(snippet, "%frequency%", frequency);
        snippet = StringUtils.replace(snippet, "%switch%", s.isSwitchEnabled() ? "1" : "0");
        snippet = StringUtils.replace(snippet, "%unit%", unit);
        snippet = StringUtils.replace(snippet, "%minValue%", minValueOf(s));
        snippet = StringUtils.replace(snippet, "%maxValue%", maxValueOf(s));
        snippet = StringUtils.replace(snippet, "%step%", stepOf(s));

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }

    private String maxValueOf(Slider slider) {
        if (slider.getMaxValue() != null) {
            return slider.getMaxValue().toString();
        }
        return "100";
    }

    private String minValueOf(Slider slider) {
        if (slider.getMinValue() != null) {
            return slider.getMinValue().toString();
        }
        return "0";
    }

    private String stepOf(Slider slider) {
        if (slider.getStep() != null) {
            return slider.getStep().toString();
        }
        return "1";
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
