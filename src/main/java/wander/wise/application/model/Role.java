package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "role_name", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    public Role(Long id) {
        this.id = id;
    }

    public enum RoleName {
        ROOT,
        ADMIN,
        USER
    }
}
