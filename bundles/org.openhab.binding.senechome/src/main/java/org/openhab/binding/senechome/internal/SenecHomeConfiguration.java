/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.senechome.internal;

/**
 * The {@link SenecHomeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Steven Schwarznau - Initial contribution
 */
public class SenecHomeConfiguration {
    public String hostname;
    public int refreshInterval = 15;
    public int limitationTresholdValue = 95;
    public int limitationDuration = 120;
}