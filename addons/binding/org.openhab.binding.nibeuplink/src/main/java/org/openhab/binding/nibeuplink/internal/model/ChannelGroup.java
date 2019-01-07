/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

/**
 * used to determine the group a channel belongs to
 *
 * @author Alexander Friese - initial contribution
 */
public enum ChannelGroup {
    BASE,
    GENERAL,
    COMPRESSOR,
    HOTWATER,
    AIRSUPPLY,
    CUSTOM;
}
