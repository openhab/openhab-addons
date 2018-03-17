/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

/**
 * type of value which is returned by the nibe uplink API. Numbers can be Integers (NUMBER) or decimals which will be
 * divided by 10 or 100.
 *
 * @author Alexander Friese - initial contribution
 *
 */
public enum ValueType {

    STRING,
    NUMBER,
    NUMBER_10,
    NUMBER_100

}
