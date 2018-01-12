/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora;

/**
 * Interface to the RadioRA Classic system
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public interface RadioRAConnection {

    public void open(String portName, int baud) throws RadioRAConnectionException;

    public void disconnect();

    public void write(String command);

    public void setListener(RadioRAFeedbackListener listener);

}
