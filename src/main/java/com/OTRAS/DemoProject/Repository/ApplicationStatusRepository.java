package com.OTRAS.DemoProject.Repository;

import com.OTRAS.DemoProject.Entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, Long> {
    List<ApplicationStatus> findByOtrasIdOrderByStatusDateTimeDesc(String otrasId);
    Optional<ApplicationStatus> findTopByOtrasIdOrderByStatusDateTimeDesc(String otrasId);
    List<ApplicationStatus> findByOtrasIdAndJobPostIdOrderByStatusDateTimeDesc(String otrasId, Long jobPostId);
}
