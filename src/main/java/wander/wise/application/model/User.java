package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "users")
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
}
