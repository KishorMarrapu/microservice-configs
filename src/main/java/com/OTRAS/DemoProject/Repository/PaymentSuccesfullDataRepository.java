package com.OTRAS.DemoProject.Repository;

import com.OTRAS.DemoProject.Entity.PaymentSuccesfullData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentSuccesfullDataRepository extends JpaRepository<PaymentSuccesfullData, Long> {
    
    boolean existsByOtrIdAndJobPostIdAndVacancyId(String otrId, Long jobPostId, Long vacancyId);
    
    PaymentSuccesfullData findByOtrId(String otrId);
    
    List<PaymentSuccesfullData> findByJobPostId(Long jobPostId);
    
   // List<PaymentSuccesfullData> findByJobPostIdAndAllocatedCenterIsNotNull(Long jobPostId);
    
    
    Optional<PaymentSuccesfullData> findById(Long id);

	Optional<PaymentSuccesfullData> findByOtrIdAndPaymentStatus(String otrId, String string);

	List<PaymentSuccesfullData> findAllByJobPostIdAndPaymentStatus(Long jobPostId, String string);

    List<PaymentSuccesfullData> findAllByCandidateProfile_Candidate_IdAndPaymentStatus(Long candidateId, String paymentStatus);

    @Query(value = "SELECT * FROM payment_succesfull_data WHERE job_post_id = :jobPostId", nativeQuery = true)
    List<PaymentSuccesfullData> findStudentsByJobPostId(Long jobPostId);

    List<PaymentSuccesfullData> findAllByCandidateProfile_IdAndPaymentStatus(
            Long candidateProfileId,
            String paymentStatus
    );

    List<PaymentSuccesfullData> findByCandidateProfile_Candidate_Id(Long candidateId);

    @Query("SELECT p FROM PaymentSuccesfullData p WHERE p.otrId = :otrId")
    PaymentSuccesfullData findByOtrIdForStatus(@Param("otrId") String otrId);
 
    }
