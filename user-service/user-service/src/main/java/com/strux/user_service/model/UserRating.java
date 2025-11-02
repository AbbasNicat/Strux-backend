package com.strux.user_service.model;
import com.strux.user_service.enums.RatingCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_ratings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId; // Rating alan user

    @Column(nullable = false)
    private String ratedBy; // Rating verən user

    @Column(nullable = false)
    private BigDecimal rating; // 1-5 arası

    @Column(length = 1000)
    private String comment;

    private String projectId; // Hansı proyektdə

    private String taskId; // Hansı tapşırıqda

    @Enumerated(EnumType.STRING)
    private RatingCategory category;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
