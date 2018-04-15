/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

import javax.measure.Quantity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MatthiasS
 *
 */
public interface MeterValueListener {

    static Logger logger = LoggerFactory.getLogger(MeterValueListener.class);

    public void errorOccoured(Throwable e);

    public <Q extends Quantity<Q>> void valueChanged(MeterValue<Q> value);

    public <Q extends Quantity<Q>> void valueRemoved(MeterValue<Q> value);
}
