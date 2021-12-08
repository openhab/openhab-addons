/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api;

import org.openhab.binding.sleepiq.api.model.Failure;

public class SleepIQException extends Exception {
    private static final long serialVersionUID = 1L;

    private final Failure failure;

    public SleepIQException(Failure failure) {
        super(failure.getError().getMessage());
        this.failure = failure;
    }

    public Failure getFailure() {
        return failure;
    }

    public SleepIQException(String message) {
        super(message);
        this.failure = null;
    }
}
