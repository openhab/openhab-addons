package org.openhab.binding.amplipi.internal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class HTTPValidationError {

    @Schema
    private List<ValidationError> detail = null;

    /**
     * Get detail
     *
     * @return detail
     **/
    @JsonProperty("detail")
    public List<ValidationError> getDetail() {
        return detail;
    }

    public void setDetail(List<ValidationError> detail) {
        this.detail = detail;
    }

    public HTTPValidationError detail(List<ValidationError> detail) {
        this.detail = detail;
        return this;
    }

    public HTTPValidationError addDetailItem(ValidationError detailItem) {
        this.detail.add(detailItem);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HTTPValidationError {\n");

        sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
