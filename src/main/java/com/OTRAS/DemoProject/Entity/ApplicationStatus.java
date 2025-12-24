package com.OTRAS.DemoProject.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String otrasId;      // OTRAS-ABC123
    
    private Long jobPostId;
    private Long candidateId;
    private String status;       // APPLIED, PAYMENT_SUCCESS, EXAM_ASSIGNED, EXAM_STARTED, SUBMITTED, RESULT_PUBLISHED
    
    private LocalDateTime statusDateTime;
    private String statusMessage;
    private String examRollNo;   // NULL until admit card
    
    @PrePersist
    private void setTimestamp() {
        this.statusDateTime = LocalDateTime.now();
    }
}
