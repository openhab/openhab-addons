/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class User {
    @SerializedName("name")
    private String name = null;

    @SerializedName("id")
    private String id = null;

    @SerializedName("email")
    private String email = null;

    @SerializedName("username")
    private String username = null;

    @SerializedName("homes")
    private List<UserHomes> homes = null;

    @SerializedName("locale")
    private String locale = null;

    public User name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User email(String email) {
        this.email = email;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User username(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User homes(List<UserHomes> homes) {
        this.homes = homes;
        return this;
    }

    public User addHomesItem(UserHomes homesItem) {
        if (this.homes == null) {
            this.homes = new ArrayList<>();
        }
        this.homes.add(homesItem);
        return this;
    }

    public List<UserHomes> getHomes() {
        return homes;
    }

    public void setHomes(List<UserHomes> homes) {
        this.homes = homes;
    }

    public User locale(String locale) {
        this.locale = locale;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(this.name, user.name) && Objects.equals(this.id, user.id)
                && Objects.equals(this.email, user.email) && Objects.equals(this.username, user.username)
                && Objects.equals(this.homes, user.homes) && Objects.equals(this.locale, user.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, email, username, homes, locale);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class User {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    email: ").append(toIndentedString(email)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    homes: ").append(toIndentedString(homes)).append("\n");
        sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
