/*
 * Copyright (c) 2013, Mathieu Velten. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines an API result that returns a single object
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class Response<T> extends BaseResponse {

    protected T result;

    public T getResult() {
        return result;
    }

    @Override
    protected @Nullable String internalEvaluate() {
        String base = super.internalEvaluate();
        return base != null ? base : ((result == null) ? "result should never be null" : null);
    }
}
