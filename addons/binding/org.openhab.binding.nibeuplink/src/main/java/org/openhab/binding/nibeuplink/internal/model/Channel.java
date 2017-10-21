/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

/**
 * interface to be implemented by all Channel Enumerations
 *
 * @author Alexander Friese - initial contribution
 *
 */
public interface Channel {

    String getName();

    String getId();

    ChannelType getChannelType();

    ChannelGroup getChannelGroup();

    Class<?> getJavaType();

    String getFQName();

}
