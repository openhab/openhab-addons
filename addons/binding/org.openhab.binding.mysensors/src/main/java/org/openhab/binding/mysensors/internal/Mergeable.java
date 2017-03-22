/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import org.openhab.binding.mysensors.internal.exception.MergeException;

/**
 * Indicates that a class could be merged to another one of the same type
 *
 * @author Andrea Cioni
 *
 */
public interface Mergeable {

    /**
     * Merge an object to another one.
     *
     * @param o
     * @throws MergeException
     */
    public void merge(Object o) throws MergeException;
}
