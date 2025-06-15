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
package org.openhab.binding.ring.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ProfileTO} class is part of the profile description
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class ProfileTO {

    @SerializedName("tfa_enabled")
    public boolean tfaEnabled;

    @SerializedName("country")
    public String country = "";

    @SerializedName("account_type")
    public String accountType = "";

    @SerializedName("tsv_state")
    public String tsvState = "";

    @SerializedName("authentication_token")
    public String authenticationToken = "";

    @SerializedName("tfa_phone_number")
    public String tfaPhoneNumber = "";

    @SerializedName("last_name")
    public String lastName = "";

    @SerializedName("created_at")
    public String createdAt = "";

    @SerializedName("app_brand")
    public String appBrand = "";

    @SerializedName("last_name_extra")
    public String lastNameExtra = "";

    @SerializedName("explorer_program_terms")
    public @Nullable Object explorerProgramTerms;

    @SerializedName("features")
    public FeaturesTO features = new FeaturesTO();

    @SerializedName("user_flow")
    public String userFlow = "";

    @SerializedName("phone_number")
    public String phoneNumber = "";

    @SerializedName("id")
    public int id;

    @SerializedName("first_name_extra")
    public String firstNameExtra = "";

    @SerializedName("first_name")
    public String firstName = "";

    @SerializedName("email")
    public String email = "";

    @SerializedName("hardware_id")
    public String hardwareId = "";

    @SerializedName("status")
    public String status = "";

    @SerializedName("cfes_enrolled")
    public boolean cfesEnrolled;

    @SerializedName("user_preferences")
    public UserPreferencesTO userPreferences = new UserPreferencesTO();
}
