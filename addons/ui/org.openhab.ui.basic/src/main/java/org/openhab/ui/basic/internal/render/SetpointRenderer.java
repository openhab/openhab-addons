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

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Setpoint;
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
 * can produce HTML code for Setpoint widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
@Component(service = WidgetRenderer.class)
public class SetpointRenderer extends AbstractWidgetRenderer {

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
        return w instanceof Setpoint;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Setpoint sp = (Setpoint) w;

        State state = itemUIRegistry.getState(w);
        String newLowerState = state.toString();
        String newHigherState = state.toString();

        // set defaults for min, max and step
        BigDecimal step = sp.getStep();
        if (step == null) {
            step = BigDecimal.ONE;
        }
        BigDecimal minValue = sp.getMinValue();
        if (minValue == null) {
            minValue = BigDecimal.ZERO;
        }
        BigDecimal maxValue = sp.getMaxValue();
        if (maxValue == null) {
            maxValue = BigDecimal.valueOf(100);
        }

        // if the current state is a valid value, we calculate the up and down step values
        if (state instanceof DecimalType) {
            DecimalType actState = (DecimalType) state;
            BigDecimal newLower = actState.toBigDecimal().subtract(step);
            BigDecimal newHigher = actState.toBigDecimal().add(step);
            if (newLower.compareTo(minValue) < 0) {
                newLower = minValue;
            }
            if (newHigher.compareTo(maxValue) > 0) {
                newHigher = maxValue;
            }
            newLowerState = newLower.toString();
            newHigherState = newHigher.toString();
        }

        String unit = getUnitForWidget(w);

        String snippetName = "setpoint";
        String snippet = getSnippet(snippetName);

        snippet = preprocessSnippet(snippet, w);
        snippet = StringUtils.replace(snippet, "%newlowerstate%", newLowerState);
        snippet = StringUtils.replace(snippet, "%newhigherstate%", newHigherState);
        snippet = StringUtils.replace(snippet, "%value%", getValue(w));
        snippet = StringUtils.replace(snippet, "%minValue%", minValue.toString());
        snippet = StringUtils.replace(snippet, "%maxValue%", maxValue.toString());
        snippet = StringUtils.replace(snippet, "%step%", step.toString());
        snippet = StringUtils.replace(snippet, "%unit%", unit);

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
