package com.finger.hand_backend.user.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;


@Entity
@Table(name = "individual_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_individual_user_user", columnNames = {"user_id"})
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "user_id", nullable = false)
    private Long userId; // users.id (1:1)


    @Column(nullable = false, length = 50)
    private String name;


    @Column(nullable = false)
    private Integer age;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('M','F')")
    private Gender gender;

    @Column(nullable = false, length = 50)
    private String job;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height; // cm


    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight; // kg


    @Column(nullable = false, length = 100)
    private String disease;


    @Column(name = "residence_type", nullable = false, length = 50)
    private String residenceType;


    @Column(name = "is_diary_reminder_enabled")
    private Boolean diaryReminderEnabled;


    @JsonFormat(pattern = "HH:mm")
    @Column(name = "notification_time")
    private LocalTime notificationTime; // HH:mm


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;


    public enum Gender { M, F }
}
