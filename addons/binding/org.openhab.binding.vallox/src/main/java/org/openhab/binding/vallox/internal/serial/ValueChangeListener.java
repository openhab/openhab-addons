/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal.serial;

/**
 * Simple Observer Interface to inform some listener
 * about changes of concrete {@link ValloxProperty}.
 * The changed value is not part of the interface. For
 * simplicity, it is only stored in the {@link ValloxStore}.
 *
 * @author Hauke Fuhrmann - Initial contribution
 *
 */
public interface ValueChangeListener {

    void notifyChanged(ValloxProperty prop);

}
