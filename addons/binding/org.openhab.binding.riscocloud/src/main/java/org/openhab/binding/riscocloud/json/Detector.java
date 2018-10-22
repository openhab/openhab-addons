
package org.openhab.binding.riscocloud.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class is generated with http://www.jsonschema2pojo.org/
 * Use json provided by MyElas server and choose these options :
 * Package : org.openhab.binding.myelas.server.handler
 * Class Name : ServerDatasObject
 * Target language : Java
 * Source type : JSON
 * Annotation style : Gson
 * Tick :
 * - Use double numbers
 * - Include getters and setters
 * - Allow additional properties
 *
 * @author Sébastien Cantineau - Initial contribution
 */

public class Detector {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("bypassed")
    @Expose
    private Boolean bypassed;
    @SerializedName("filter")
    @Expose
    private String filter;
    @SerializedName("classAttrib")
    @Expose
    private String classAttrib;
    @SerializedName("data_icon")
    @Expose
    private String dataIcon;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("strTimeval")
    @Expose
    private String strTimeval;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getBypassed() {
        return bypassed;
    }

    public void setBypassed(Boolean bypassed) {
        this.bypassed = bypassed;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getClassAttrib() {
        return classAttrib;
    }

    public void setClassAttrib(String classAttrib) {
        this.classAttrib = classAttrib;
    }

    public String getDataIcon() {
        return dataIcon;
    }

    public void setDataIcon(String dataIcon) {
        this.dataIcon = dataIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrTimeval() {
        return strTimeval;
    }

    public void setStrTimeval(String strTimeval) {
        this.strTimeval = strTimeval;
    }

}
