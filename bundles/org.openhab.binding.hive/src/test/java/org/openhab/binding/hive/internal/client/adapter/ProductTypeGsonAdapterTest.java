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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.ProductType;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class ProductTypeGsonAdapterTest extends ComplexEnumGsonAdapterTest<ProductType, ProductTypeGsonAdapter> {
    @Override
    protected ProductTypeGsonAdapter getAdapter() {
        return new ProductTypeGsonAdapter();
    }

    @Override
    protected List<List<Object>> getGoodParams() {
        return Arrays.asList(
                Arrays.asList(ProductType.ACTIONS, HiveApiConstants.PRODUCT_TYPE_ACTIONS),
                Arrays.asList(ProductType.BOILER_MODULE, HiveApiConstants.PRODUCT_TYPE_BOILER_MODULE),
                Arrays.asList(ProductType.DAYLIGHT_SD, HiveApiConstants.PRODUCT_TYPE_DAYLIGHT_SD),
                Arrays.asList(ProductType.HEATING, HiveApiConstants.PRODUCT_TYPE_HEATING),
                Arrays.asList(ProductType.HOT_WATER, HiveApiConstants.PRODUCT_TYPE_HOT_WATER),
                Arrays.asList(ProductType.HUB, HiveApiConstants.PRODUCT_TYPE_HUB),
                Arrays.asList(ProductType.THERMOSTAT_UI, HiveApiConstants.PRODUCT_TYPE_THERMOSTAT_UI),
                Arrays.asList(ProductType.TRV, HiveApiConstants.PRODUCT_TYPE_TRV),
                Arrays.asList(ProductType.TRV_GROUP, HiveApiConstants.PRODUCT_TYPE_TRV_GROUP),
                Arrays.asList(ProductType.UNKNOWN, HiveApiConstants.PRODUCT_TYPE_UNKNOWN)
        );
    }

    @Override
    protected ProductType getUnexpectedEnum() {
        return ProductType.UNEXPECTED;
    }

    @Override
    protected String getUnexpectedString() {
        return "SOMETHING_UNEXPECTED";
    }
}
