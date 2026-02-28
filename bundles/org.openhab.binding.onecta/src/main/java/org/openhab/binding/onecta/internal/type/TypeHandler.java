/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.onecta.internal.type;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;

/**
 *
 * This class is responsible for transforming to a State type
 *
 * @author Alexander Drent - Initial contribution
 *
 */
public class TypeHandler {

    public static State stringType(@Nullable String value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value);
    }

    public static State stringType(Enums.DemandControl value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.FanMovement value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.FanMovementVer value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.FanMovementHor value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(JsonObject value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.OperationMode value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.SetpointMode value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.FanSpeed value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State stringType(Enums.HeatupMode value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.toString());
    }

    public static State decimalType(@Nullable Number value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new DecimalType(value);
    }

    public static State dateTimeType(@Nullable ZonedDateTime value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(value);
    }

    public static State onOffType(@Nullable String value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return OnOffType.from(value);
    }

    public static State onOffType(@Nullable Boolean value) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return OnOffType.from(value);
    }
}
