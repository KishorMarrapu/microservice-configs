package com.OTRAS.DemoProject.Service;
 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.OTRAS.DemoProject.DTO.AdmitCardDTO;
import com.OTRAS.DemoProject.Entity.AdmitCard;
import com.OTRAS.DemoProject.Entity.CandidateProfile;
import com.OTRAS.DemoProject.Entity.JobPost;
import com.OTRAS.DemoProject.Entity.PaymentSuccesfullData;
import com.OTRAS.DemoProject.Entity.VacancyDetails;
import com.OTRAS.DemoProject.Repository.AdmitCardRepository;
import com.OTRAS.DemoProject.Repository.JobPostRepository;
import com.OTRAS.DemoProject.Repository.PaymentSuccesfullDataRepository;
import com.OTRAS.DemoProject.Repository.VacancyRepository;

import lombok.RequiredArgsConstructor;
 
@Service

@RequiredArgsConstructor

public class AdmitCardService {
 
    private final AdmitCardRepository admitCardRepo;

    private final PaymentSuccesfullDataRepository paymentRepo;

    private final JobPostRepository jobPostRepo;

    private final VacancyRepository vacancyRepo;
 
    @Transactional

    public AdmitCardDTO generateAdmitCard(Long paymentId) {
 
        PaymentSuccesfullData payment = paymentRepo.findById(paymentId)

                .orElseThrow(() -> new RuntimeException("Payment data not found"));
 
        if (!"SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {

            throw new RuntimeException("Admit Card allowed only for payment success users");

        }
 

        AdmitCard admitCard = admitCardRepo.findByPaymentSuccesfullData(payment).orElse(null);

        if (admitCard == null) {

            CandidateProfile profile = payment.getCandidateProfile();

            String examCenter = payment.getCenters() != null && !payment.getCenters().isEmpty()

                    ? payment.getCenters().get(0)

                    : "Center Not Assigned";
 
            admitCard = AdmitCard.builder()

                    .paymentSuccesfullData(payment)

                    .examRollNo(payment.getExamRollNo())

                    .candidateName(profile.getCandidateName())

                    .fatherName(profile.getFathersName())

                    .gender(profile.getGender())

                    .dateOfBirth(profile.getDateOfBirth())

                    .examCenter(examCenter)

                    .collegeName(" Vignan  College")

                    .universityName("Vignan University")

                    .otrasId(payment.getOtrId())

                    .build();
 
            admitCard = admitCardRepo.save(admitCard);

        }
 
        String vacancyName;

        if (payment.getVacancyId() != null) {

            Optional<VacancyDetails> vacancyOpt = vacancyRepo.findById(payment.getVacancyId());

            vacancyName = vacancyOpt.map(VacancyDetails::getPostName).orElse("Unknown Vacancy");

        } else {

            vacancyName = "Unknown Vacancy";

        }
 
        String jobPostName;

        if (payment.getJobPostId() != null) {

            Optional<JobPost> jobOpt = jobPostRepo.findById(payment.getJobPostId());

            jobPostName = jobOpt.map(JobPost::getJobTitle).orElse("Unknown Job");

        } else {

            jobPostName = "Unknown Job";

        }
 
 
        return AdmitCardDTO.builder()

                .examRollNo(admitCard.getExamRollNo())

                .candidateName(admitCard.getCandidateName())

                .fatherName(admitCard.getFatherName())

                .gender(admitCard.getGender())

                .dateOfBirth(admitCard.getDateOfBirth())

                .examCenter(admitCard.getExamCenter())

                .collegeName(admitCard.getCollegeName())

                .universityName(admitCard.getUniversityName())

                .jobPostName(jobPostName)

                .vacancyName(vacancyName)

                .otrasId(admitCard.getOtrasId())

                .build();

    }


//    @Transactional
//    public List<AdmitCardDTO> generateAllByJobPost(Long jobPostId) {
//        List<PaymentSuccesfullData> payments = paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");
//
//        List<AdmitCardDTO> admitCards = new ArrayList<>();
//
//        for (PaymentSuccesfullData payment : payments) {
//            AdmitCard admitCard = admitCardRepo.findByPaymentSuccesfullData(payment)
//                    .orElseGet(() -> createAdmitCard(payment));
//
//            admitCards.add(mapToDTO(admitCard, payment));
//        }
//
//        return admitCards;
//    }

//    @Transactional(readOnly = true)
//    public List<AdmitCardDTO> generateAllByJobPost(Long jobPostId) {
//        // ✅ Step 1: Fetch only SUCCESS payments
//        List<PaymentSuccesfullData> payments =
//                paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");
//
//        // ✅ Step 2: Validation — no successful payments found
//        if (payments == null || payments.isEmpty()) {
//            throw new IllegalStateException("No successful payments found for this job post.");
//        }
//
//        // ✅ Step 3: Map to AdmitCardDTOs
//        List<AdmitCardDTO> admitCards = new ArrayList<>();
//
//        for (PaymentSuccesfullData payment : payments) {
//            admitCardRepo.findByPaymentSuccesfullData(payment).ifPresent(admitCard -> {
//                admitCards.add(mapToDTO(admitCard, payment));
//            });
//        }
//
//        // ✅ Step 4: Validation — if payments exist but no admit cards yet
//        if (admitCards.isEmpty()) {
//            throw new IllegalStateException("No admit cards have been generated yet for this job post.");
//        }
//
//        return admitCards;
//    }
//    @Transactional
//    public List<AdmitCardDTO> generateAllByJobPost(Long jobPostId) {
//        List<PaymentSuccesfullData> payments =
//                paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");
//
//        if (payments == null || payments.isEmpty()) {
//            throw new IllegalStateException("No successful payments found for this job post.");
//        }
//
//        List<AdmitCardDTO> admitCards = new ArrayList<>();
//
//        for (PaymentSuccesfullData payment : payments) {
//            // ✅ Check if already exists
//            AdmitCard admitCard = admitCardRepo.findByPaymentSuccesfullData(payment).orElseGet(() -> {
//                // ✅ If not, create a new one
//                AdmitCard newAdmit = AdmitCard.builder()
//                        .paymentSuccesfullData(payment)
//                       // .admitCardNumber("ADMIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
//                        //.issueDate(LocalDate.now())
//                        .build();
//                return admitCardRepo.save(newAdmit);
//            });
//
//            admitCards.add(mapToDTO(admitCard, payment));
//        }
//
//        return admitCards;
//    }

    @Transactional
    public List<AdmitCardDTO> generateAllByJobPost(Long jobPostId) {
        // ✅ Step 1: Fetch only SUCCESS payments
        List<PaymentSuccesfullData> payments =
                paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");

        if (payments == null || payments.isEmpty()) {
            throw new IllegalStateException("No successful payments found for this job post.");
        }

        List<AdmitCardDTO> admitCards = new ArrayList<>();

        for (PaymentSuccesfullData payment : payments) {

            //  Check if admit card already exists for this payment
            Optional<AdmitCard> existing = admitCardRepo.findByPaymentSuccesfullData(payment);

            AdmitCard admitCard;
            if (existing.isPresent()) {
                admitCard = existing.get(); // use existing one
            } else {
                //  Create a new admit card for this successful payment
                admitCard = AdmitCard.builder()
                        .examRollNo(payment.getExamRollNo())
                        .candidateName(payment.getCandidateProfile().getCandidateName())
                        .fatherName(payment.getCandidateProfile().getFathersName())
                        .gender(payment.getCandidateProfile().getGender())
                        .dateOfBirth(payment.getCandidateProfile().getDateOfBirth())
                        .examCenter(payment.getCenters().isEmpty() ? "N/A" : payment.getCenters().get(0))
                        .paymentSuccesfullData(payment)
                        .otrasId(payment.getOtrId())
                        .build();

                admitCardRepo.save(admitCard);
            }

            admitCards.add(mapToDTO(admitCard, payment));
        }

        return admitCards;
    }

 

    public List<AdmitCardDTO> getAllAdmitCardsByJob(Long jobPostId) {
 
        List<PaymentSuccesfullData> payments = paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");

        if(payments.isEmpty()) {
        	throw new RuntimeException  ("No Payments Found for This Category");
        }
        
        List<AdmitCardDTO> admitCards = new ArrayList<>();
 
        for (PaymentSuccesfullData payment : payments) {

            admitCardRepo.findByPaymentSuccesfullData(payment).ifPresent(admitCard -> {

                admitCards.add(mapToDTO(admitCard, payment));

            });

        }
 
        return admitCards;

    }
 

    public AdmitCardDTO getAdmitCardByOtrId(String otrId) {
 
        PaymentSuccesfullData payment = paymentRepo.findByOtrIdAndPaymentStatus(otrId, "SUCCESS")

                .orElseThrow(() -> new RuntimeException("Payment not found or not successful"));
 
        AdmitCard admitCard = admitCardRepo.findByPaymentSuccesfullData(payment)

                .orElseThrow(() -> new RuntimeException("Admit card not generated yet"));
 
        return mapToDTO(admitCard, payment);

    }
 

    private AdmitCard createAdmitCard(PaymentSuccesfullData payment) {

        CandidateProfile profile = payment.getCandidateProfile();

        String examCenter = payment.getCenters() != null && !payment.getCenters().isEmpty()

                ? payment.getCenters().get(0)

                : "Center Not Assigned";
 
        AdmitCard admitCard = AdmitCard.builder()

                .paymentSuccesfullData(payment)

                .examRollNo(payment.getExamRollNo())

                .candidateName(profile.getCandidateName())

                .fatherName(profile.getFathersName())

                .gender(profile.getGender())

                .dateOfBirth(profile.getDateOfBirth())

                .examCenter(examCenter)

                .collegeName("National Public College")

                .universityName("State Public University")

                .otrasId(payment.getOtrId())

                .build();
 
        return admitCardRepo.save(admitCard);

    }
 

    private AdmitCardDTO mapToDTO(AdmitCard admitCard, PaymentSuccesfullData payment) {

        String vacancyName = payment.getVacancyId() != null

                ? vacancyRepo.findById(payment.getVacancyId()).map(VacancyDetails::getPostName).orElse("Unknown Vacancy")

                : "Unknown Vacancy";
 
        String jobPostName = payment.getJobPostId() != null

                ? jobPostRepo.findById(payment.getJobPostId()).map(JobPost::getJobTitle).orElse("Unknown Job")

                : "Unknown Job";
 
        return AdmitCardDTO.builder()

                .examRollNo(admitCard.getExamRollNo())

                .candidateName(admitCard.getCandidateName())

                .fatherName(admitCard.getFatherName())

                .gender(admitCard.getGender())

                .dateOfBirth(admitCard.getDateOfBirth())

                .examCenter(admitCard.getExamCenter())

                .collegeName(admitCard.getCollegeName())

                .universityName(admitCard.getUniversityName())

                .jobPostName(jobPostName)

                .vacancyName(vacancyName)

                .otrasId(payment.getOtrId())

                .build();

    }
 
    
    @Transactional(readOnly = true)
    public List<AdmitCardDTO> getAllAdmitCardsByCandidate(Long candidateProfileId) {

        //  Step 1: Fetch all SUCCESS payments for candidate profile
        List<PaymentSuccesfullData> payments =
            paymentRepo.findAllByCandidateProfile_IdAndPaymentStatus(candidateProfileId, "SUCCESS");

        if (payments == null || payments.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No successful payment records found for candidate profile ID: " + candidateProfileId
            );
        }

        List<AdmitCardDTO> admitCards = new ArrayList<>();

        //  Step 2: Loop and collect only those that have AdmitCards
        for (PaymentSuccesfullData payment : payments) {
            admitCardRepo.findByPaymentSuccesfullData(payment)
                .ifPresent(admitCard -> admitCards.add(mapToDTO(admitCard, payment)));
        }

        //  Step 3: If none found, return friendly message
        if (admitCards.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No admit cards generated yet for candidate profile ID: " + candidateProfileId
            );
        }

        return admitCards;
    }
}

 