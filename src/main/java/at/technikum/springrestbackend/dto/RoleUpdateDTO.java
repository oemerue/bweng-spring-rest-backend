package at.technikum.springrestbackend.dto;

import at.technikum.springrestbackend.entity.Role;
import jakarta.validation.constraints.NotNull;

public class RoleUpdateDTO {

    @NotNull(message = "Role is required")
    private Role role;

    public RoleUpdateDTO() {
    }

    public RoleUpdateDTO(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}