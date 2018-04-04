/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal;

import org.openhab.binding.foxtrot.internal.plccoms.PlcComSReply;

/**
 * Refreshable.
 *
 * @author Radovan Sninsky
 * @since 2018-02-15 20:23
 */
public interface Refreshable {

    void refresh(PlcComSReply reply);
}
