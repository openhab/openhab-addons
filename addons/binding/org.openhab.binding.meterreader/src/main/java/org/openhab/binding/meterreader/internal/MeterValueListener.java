/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MatthiasS
 *
 */
public interface MeterValueListener {

    static Logger logger = LoggerFactory.getLogger(MeterValueListener.class);

    public default void errorOccoured(Exception e) {
        logger.error("", e);
    }

    public void valueChanged(MeterValue value);

    public void valueRemoved(MeterValue value);
}
