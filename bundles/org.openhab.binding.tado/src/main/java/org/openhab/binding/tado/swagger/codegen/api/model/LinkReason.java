package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class LinkReason {
    @SerializedName("code")
    private String code = null;

    @SerializedName("title")
    private String title = null;

    public LinkReason code(String code) {
        this.code = code;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LinkReason title(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkReason linkReason = (LinkReason) o;
        return Objects.equals(this.code, linkReason.code) && Objects.equals(this.title, linkReason.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinkReason {\n");

        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
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
