package wander.wise.application.dto.user.update;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserRolesRequestDto(@NotEmpty Set<Long> roleIds) {
}
