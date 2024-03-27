package wander.wise.application.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "filter_sets")
@Setter
@Getter
@RequiredArgsConstructor
@SoftDelete
public class FilterSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    //filters = "atmosphere:lounge,calm,warm|climate:hot|company:5 persons|
    // children:with children|pets:with dog,with cat"
    @Column(nullable = false)
    private String filters;
    @Column(nullable = false)
    private Long usages;
}
