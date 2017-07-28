/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.mappers;


import java.lang.reflect.Type;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public interface JsonMapper {
    String map(Object o);

    <T> T to(Class<T> clazz, String string);

    <T> T to(Type clazz, String string);
}
