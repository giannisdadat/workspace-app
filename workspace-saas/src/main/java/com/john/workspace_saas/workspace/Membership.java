package com.john.workspace_saas.workspace;

import com.john.workspace_saas.user.user;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workspace_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private user user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}