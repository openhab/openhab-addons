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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Selection;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Selection widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(service = WidgetRenderer.class)
public class SelectionRenderer extends AbstractWidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(SelectionRenderer.class);

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Selection;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        String snippet = getSnippet("selection");

        snippet = StringUtils.replace(snippet, "%category%", getCategory(w));
        snippet = StringUtils.replace(snippet, "%state%", getState(w));
        snippet = StringUtils.replace(snippet, "%format%", getFormat());

        State state = itemUIRegistry.getState(w);
        Selection selection = (Selection) w;
        String mappedValue = "";
        String rowMappedValue;

        Item item = null;
        try {
            item = itemUIRegistry.getItem(w.getItem());
        } catch (ItemNotFoundException e) {
            logger.debug("Failed to retrieve item during widget rendering: {}", e.getMessage());
        }

        StringBuilder rowSB = new StringBuilder();
        if (selection.getMappings().size() == 0 && item != null) {
            final StateDescription stateDescription = item.getStateDescription();
            if (stateDescription != null) {
                for (StateOption option : stateDescription.getOptions()) {
                    rowMappedValue = buildRow(selection, option.getLabel(), option.getValue(), item, state, rowSB);
                    if (rowMappedValue != null) {
                        mappedValue = rowMappedValue;
                    }
                }
            }
        } else {
            for (Mapping mapping : selection.getMappings()) {
                rowMappedValue = buildRow(selection, mapping.getLabel(), mapping.getCmd(), item, state, rowSB);
                if (rowMappedValue != null) {
                    mappedValue = rowMappedValue;
                }
            }
        }
        snippet = StringUtils.replace(snippet, "%label_header%", getLabel(w, mappedValue));
        snippet = StringUtils.replace(snippet, "%rows%", rowSB.toString());

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }

    private String buildRow(Selection w, String lab, String cmd, Item item, State state, StringBuilder rowSB)
            throws RenderException {
        String mappedValue = null;
        String rowSnippet = getSnippet("selection_row");

        String command = cmd != null ? cmd : "";
        String label = lab;

        if (item instanceof NumberItem && ((NumberItem) item).getDimension() != null) {
            String unit = getUnitForWidget(w);
            command = StringUtils.replace(command, UnitUtils.UNIT_PLACEHOLDER, unit);
            label = StringUtils.replace(label, UnitUtils.UNIT_PLACEHOLDER, unit);
        }

        rowSnippet = StringUtils.replace(rowSnippet, "%item%", w.getItem() != null ? w.getItem() : "");
        rowSnippet = StringUtils.replace(rowSnippet, "%cmd%", StringEscapeUtils.escapeHtml(command));
        rowSnippet = StringUtils.replace(rowSnippet, "%label%",
                label != null ? StringEscapeUtils.escapeHtml(label) : "");

        State compareMappingState = state;
        if (state instanceof QuantityType) { // convert the item state to the command value for proper
                                             // comparison and "checked" attribute calculation
            compareMappingState = convertStateToLabelUnit((QuantityType<?>) state, command);
        }

        if (compareMappingState.toString().equals(command)) {
            mappedValue = label;
            rowSnippet = StringUtils.replace(rowSnippet, "%checked%", "checked=\"true\"");
        } else {
            rowSnippet = StringUtils.replace(rowSnippet, "%checked%", "");
        }

        rowSB.append(rowSnippet);

        return mappedValue;
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
