/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal;

/**
 * Interface for classes that handle Readings.
 *
 * @author Volker Bier - Initial contribution
 */
public interface ReadingHandler<R extends Reading> {
    public void handleReading(R r);

    public String getSketchName();
}
