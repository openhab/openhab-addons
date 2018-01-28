/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.handler;

import org.syphr.lametrictime.api.local.model.Widget;

/**
 * The {@link LaMetricTimeAppHandler} provides a common contract for all app handlers available for the device.
 *
 * @author Gregory Moyer - Initial contribution
 */
public interface LaMetricTimeAppHandler {

    /**
     * Retrieve the LaMetric Time app instance ({@link Widget}) associated with this handler.
     *
     * @return the {@link Widget}
     */
    public Widget getWidget();
}
