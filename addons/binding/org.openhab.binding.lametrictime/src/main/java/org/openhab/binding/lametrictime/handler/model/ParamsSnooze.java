/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.handler.model;

import java.time.temporal.ChronoUnit;

/**
 * This class represents the parameter arguments for the clock app snooze action.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class ParamsSnooze {

    public long amount;
    public ChronoUnit unit;
}
