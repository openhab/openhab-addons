/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.handler;

/**
 * Interface for callback from Client handler to Slot handler.
 *
 * The client performs refreshes regularly, retrieving information about
 * all slots. It then calls refreshed on all registered SlotUpdateListeners.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
public interface SlotUpdateListener {
    void refreshed(SlotInfo si);
}
