/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * Login response from auth/login endpoint
 *
 * @author Dan Cunningham - Initial contribution
 */
public class LoginResponse {
    @SerializedName("unique_id")
    public String uniqueId;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    public String alias;

    @SerializedName("full_name")
    public String fullName;

    public String email;

    @SerializedName("email_status")
    public String emailStatus;

    @SerializedName("email_is_null")
    public Boolean emailIsNull;

    @SerializedName("user_email")
    public String userEmail;

    public String phone;

    @SerializedName("avatar_relative_path")
    public String avatarRelativePath;

    @SerializedName("avatar_rpath2")
    public String avatarRpath2;

    public String status;

    @SerializedName("employee_number")
    public String employeeNumber;

    @SerializedName("create_time")
    public Long createTime;

    @SerializedName("login_time")
    public Long loginTime;

    public Map<String, Object> extras;

    public String username;

    @SerializedName("local_account_exist")
    public Boolean localAccountExist;

    @SerializedName("password_revision")
    public Long passwordRevision;

    @SerializedName("sso_account")
    public String ssoAccount;

    @SerializedName("sso_uuid")
    public String ssoUuid;

    @SerializedName("sso_username")
    public String ssoUsername;

    @SerializedName("sso_picture")
    public String ssoPicture;

    @SerializedName("uid_sso_id")
    public String uidSsoId;

    @SerializedName("uid_sso_account")
    public String uidSsoAccount;

    @SerializedName("uid_account_status")
    public String uidAccountStatus;

    public List<Group> groups;
    public List<Role> roles;
    public Map<String, List<String>> permissions;
    public List<String> scopes;

    @SerializedName("cloud_access_granted")
    public Boolean cloudAccessGranted;

    @SerializedName("only_local_account")
    public Boolean onlyLocalAccount;

    @SerializedName("update_time")
    public Long updateTime;

    public Object avatar;

    @SerializedName("nfc_token")
    public String nfcToken;

    @SerializedName("nfc_display_id")
    public String nfcDisplayId;

    @SerializedName("nfc_card_type")
    public String nfcCardType;

    @SerializedName("nfc_card_status")
    public String nfcCardStatus;

    @SerializedName("org_user_id")
    public String orgUserId;

    public String role;
    public String roleId;

    public String id;

    public Boolean isOwner;
    public Boolean isSuperAdmin;
    public Boolean isMember;

    public String maskedEmail;
    public Integer accessMask;
    public Integer permissionMask;

    public UcorePermission ucorePermission;

    public String deviceToken;
    public Map<String, Object> ssoAuth;

    public static class Group {
        @SerializedName("unique_id")
        public String uniqueId;

        public String name;

        @SerializedName("up_id")
        public String upId;

        @SerializedName("up_ids")
        public List<String> upIds;

        @SerializedName("system_name")
        public String systemName;

        @SerializedName("create_time")
        public String createTime;
    }

    public static class Role {
        @SerializedName("unique_id")
        public String uniqueId;

        public String name;

        @SerializedName("system_role")
        public Boolean systemRole;

        @SerializedName("system_key")
        public String systemKey;

        public Integer level;

        @SerializedName("create_time")
        public String createTime;

        @SerializedName("update_time")
        public String updateTime;

        @SerializedName("is_private")
        public Boolean isPrivate;
    }

    public static class UcorePermission {
        public Boolean hasUpdateAndInstallPermission;
    }
}
