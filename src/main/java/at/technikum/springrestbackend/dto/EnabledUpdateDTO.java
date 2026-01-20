package at.technikum.springrestbackend.dto;

import jakarta.validation.constraints.NotNull;

public class EnabledUpdateDTO {

    @NotNull(message = "enabled is required")
    private Boolean enabled;

    public EnabledUpdateDTO() {
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
