package com.finger.hand_backend.group.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp; import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Getter @Setter
@Entity @Table(name="hand_group",
        uniqueConstraints=@UniqueConstraint(name="uq_groups_invite_code", columnNames="inviteCode"))
public class Group {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100) private String name;
    @Column(nullable=false, length=50)  private String groupType;
    @Column(nullable=false, length=6)   private String inviteCode;
    @Column(nullable=false)             private Long createdBy;
    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp  private Instant updatedAt;
}
