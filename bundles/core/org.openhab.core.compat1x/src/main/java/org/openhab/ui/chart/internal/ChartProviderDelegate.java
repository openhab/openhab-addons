/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.chart.internal;

import java.awt.image.BufferedImage;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.openhab.ui.chart.ChartProvider;


/**
 * This class serves as a mapping from the "old" org.openhab namespace to the new org.eclipse.smarthome
 * namespace for the action service. It wraps an instance with the old interface
 * into a class with the new interface. 
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class ChartProviderDelegate implements org.eclipse.smarthome.ui.chart.ChartProvider {

	private ChartProvider provider;

	public ChartProviderDelegate(ChartProvider chartProvider) {
		this.provider = chartProvider;
	}

	@Override
	public String getName() {
		return provider.getName();
	}

    @Override
    public BufferedImage createChart(String service, String theme, Date startTime, Date endTime, int height, int width,
            String items, String groups) throws ItemNotFoundException {
        try {
            return provider.createChart(service, theme, startTime, endTime, height, width, items, groups);
        } catch (org.openhab.core.items.ItemNotFoundException e) {
            throw new ItemNotFoundException(StringUtils.substringBetween(e.getMessage(), "'"));
        }
    }

    @Override
    public ImageType getChartType() {
        return ImageType.valueOf(provider.getChartType().name());
    }

}
