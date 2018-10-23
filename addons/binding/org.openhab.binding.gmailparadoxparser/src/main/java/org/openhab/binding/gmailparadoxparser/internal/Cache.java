/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gmailparadoxparser.internal;

import java.util.List;

/**
 * @author Konstantin Polihronov - Initial contribution
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K, V> {
    void put(K key, V value);

    V get(K key);

    void refresh(List<String> retrievedMessages);
}
