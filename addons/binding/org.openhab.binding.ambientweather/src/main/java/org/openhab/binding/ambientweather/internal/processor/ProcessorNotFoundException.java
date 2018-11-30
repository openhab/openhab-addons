/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.processor;

/**
 * The {@link ProcessorNotFoundException} is thrown when a data event is
 * received for which there's no processor that can process the event.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ProcessorNotFoundException extends Exception {
    private static final long serialVersionUID = -7522973666620330850L;

    public ProcessorNotFoundException(String message) {
        super(message);
    }
}
