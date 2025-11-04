package com.finger.hand_backend.group.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Getter @Setter
@Entity
@Table(name="group_members",
        uniqueConstraints=@UniqueConstraint(name="uq_group_user", columnNames={"group_id","userId"}))
public class GroupMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="group_id", nullable=false, foreignKey=@ForeignKey(name="fk_group_members_group"))
    private Group group;

    @Column(nullable=false) private Long userId; // User 엔티티 연결 없이 ID만
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10)
    private GroupRole role = GroupRole.MANAGER; // 생성자는 관리자

    @Column(nullable=false, length=2000) private String specialNotes = "";
    @CreationTimestamp private Instant joinedAt;
}
