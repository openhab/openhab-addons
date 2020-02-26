/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.ProductType;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link ProductType}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ProductTypeGsonAdapter extends ComplexEnumGsonTypeAdapterBase<ProductType> {
    public ProductTypeGsonAdapter() {
        super(EnumMapper.builder(ProductType.class)
                .setUnexpectedValue(ProductType.UNEXPECTED)
                .add(ProductType.ACTIONS, HiveApiConstants.PRODUCT_TYPE_ACTIONS)
                .add(ProductType.BOILER_MODULE, HiveApiConstants.PRODUCT_TYPE_BOILER_MODULE)
                .add(ProductType.DAYLIGHT_SD, HiveApiConstants.PRODUCT_TYPE_DAYLIGHT_SD)
                .add(ProductType.HEATING, HiveApiConstants.PRODUCT_TYPE_HEATING)
                .add(ProductType.HOT_WATER, HiveApiConstants.PRODUCT_TYPE_HOT_WATER)
                .add(ProductType.HUB, HiveApiConstants.PRODUCT_TYPE_HUB)
                .add(ProductType.THERMOSTAT_UI, HiveApiConstants.PRODUCT_TYPE_THERMOSTAT_UI)
                .add(ProductType.TRV, HiveApiConstants.PRODUCT_TYPE_TRV)
                .add(ProductType.TRV_GROUP, HiveApiConstants.PRODUCT_TYPE_TRV_GROUP)
                .add(ProductType.UNKNOWN, HiveApiConstants.PRODUCT_TYPE_UNKNOWN)
                .build());
    }
}
