package org.apache.streampipes.model.labeling;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.streampipes.model.shared.annotation.TsModel;

import java.util.List;

@TsModel
public class Category {
    private String name;
    private String internalName;
    private String superLabelId;

    @JsonProperty("_id")
    private @SerializedName("_id") String id;

    @JsonProperty("_rev")
    private @SerializedName("_rev") String rev;

    public Category() { }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getInternalName() {
        return internalName;
    }
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getSuperLabelId() { return superLabelId; }
    public void setSuperLabel(String superLabelId) { this.superLabelId = superLabelId; }

    public String getId() {
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getRev() {
        return rev;
    }
    public void setRev(String rev) {
        this.rev = rev;
    }
}
