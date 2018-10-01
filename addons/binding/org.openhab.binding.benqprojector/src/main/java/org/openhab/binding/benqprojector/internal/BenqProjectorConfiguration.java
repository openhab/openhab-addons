/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.benqprojector.internal;

/**
 * The {@link BenqProjectorConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author René Treffer - Initial contribution
 */
public class BenqProjectorConfiguration {

    public String serialPort;

    public int serialSpeed = 115200;

    public int refreshInterval = 60;
}
