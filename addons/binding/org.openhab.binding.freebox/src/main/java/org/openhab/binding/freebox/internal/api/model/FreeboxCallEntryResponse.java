/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.api.model;

import java.util.List;

/**
 * The {@link FreeboxCallEntryResponse} is the Java class used to map the
 * response of the call API
 * https://dev.freebox.fr/sdk/os/call/#
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxCallEntryResponse extends FreeboxResponse<List<FreeboxCallEntry>> {
}
