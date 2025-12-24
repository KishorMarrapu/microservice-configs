package com.OTRAS.DemoProject.Controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.OTRAS.DemoProject.DTO.CandidateProfileResponseDTO;
import com.OTRAS.DemoProject.Entity.CandidateProfile;
import com.OTRAS.DemoProject.Entity.PaymentSuccesfullData;
import com.OTRAS.DemoProject.Mapper.CandidateProfileMapper;
import com.OTRAS.DemoProject.Repository.CandidateProfileRepository;
import com.OTRAS.DemoProject.Repository.PaymentSuccesfullDataRepository;
import com.OTRAS.DemoProject.Service.ExamAssignmentService;
import com.OTRAS.DemoProject.Util.FileUploadUtility; // Assuming this utility exists
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
	
	@Autowired
	private ExamAssignmentService examAssignmentService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    FileUploadUtility fileUploadUtility; 
    
    @Autowired
    private CandidateProfileRepository candidateProfileRepository;
    
    @Autowired
    private PaymentSuccesfullDataRepository dataRepository;

    @PostMapping("/create-checkout-session")
    public String createCheckoutSession(@RequestBody PaymentRequest req) throws Exception {

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(req.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl(req.getCancelUrl())
                        .addLineItem(
                           SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("inr")
                                                        .setUnitAmount(req.getAmount())
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName(req.getProductName())
                                                                        .build())
                                                        .build())
                                        .build())
                        .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
    
    @PostMapping(value = "/application/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitApplication(
    		@RequestParam String candidateProfileId,
            @RequestParam String otrId,
            @RequestParam Long jobPostId,
            @RequestParam Long vacancyId,   
            @RequestParam String centers,
            @RequestParam("livePhoto") MultipartFile livePhoto,
            @RequestParam("signature") MultipartFile signature
    ) throws IOException {
    	
    	System.out.println("======================================OTRId"+otrId);
    	

        if (dataRepository.existsByOtrIdAndJobPostIdAndVacancyId(otrId, jobPostId, vacancyId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already applied for this specific vacancy under this job.");
        }

        CandidateProfile candidateProfile = candidateProfileRepository.findByOtrasId(otrId);
        if (candidateProfile == null) {
            return ResponseEntity.badRequest().body("Candidate Profile Not Found. Complete profile first.");
        }
       
      
    	String livePhotoFile=fileUploadUtility.uploadFile(livePhoto, "OTRAS", otrId + "-photo");
    	String signatureFile=fileUploadUtility.uploadFile(signature, "OTRAS", otrId + "-signature");
        
        List<String> centerList = new ObjectMapper().readValue(centers, List.class);
        
        String rollNo = "OTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PaymentSuccesfullData data=PaymentSuccesfullData.builder()
        		.otrId(otrId)
        		.jobPostId(jobPostId)
        		.vacancyId(vacancyId)
        		.livePhoto(livePhotoFile)
        		.signatureFile(signatureFile)
                .examRollNo(rollNo) 
        		.centers(centerList)
        		.paymentStatus("SUCCESS")
        		
        		.candidateProfile(candidateProfile)
        		.build();
        
        dataRepository.save(data);
        
        examAssignmentService.updateApplicationStatus(otrId, jobPostId, "APPLIED", "Application submitted successfully with exam roll no: " + rollNo);
        return ResponseEntity.ok("Application Submitted Successfully");
    }
    
    @GetMapping("/getCandidateDetails")
    public ResponseEntity<?> getCandidateDetails(@RequestParam String otrId) {
    	
    	PaymentSuccesfullData data=dataRepository.findByOtrId(otrId);
    	
        CandidateProfile profile = candidateProfileRepository.findByOtrasId(otrId);

        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student not found with otrId: " + otrId);
        }

        return ResponseEntity.ok(CandidateProfileMapper.toDTO(profile,data));
    }
    
    @GetMapping("/getAllCandidateProfiles")
    public ResponseEntity<?> getAllCandidateProfiles() {
        List<CandidateProfile> profiles = candidateProfileRepository.findAll();

        if (profiles == null || profiles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Candidate Profiles Found");
        }

        List<CandidateProfileResponseDTO> dtoList = profiles.stream()
            .map(profile -> {
                PaymentSuccesfullData data = dataRepository.findByOtrId(profile.getOtrasId());
                return CandidateProfileMapper.toDTO(profile, data);
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/application/check")
    public ResponseEntity<?> checkExistingApplication(
            @RequestParam String otrId,
            @RequestParam Long jobPostId,
            @RequestParam Long vacancyId) {
        
        boolean exists = dataRepository.existsByOtrIdAndJobPostIdAndVacancyId(otrId, jobPostId, vacancyId);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("alreadyApplied", exists);
        
        if (exists) {
            response.put("message", "You have already applied for this vacancy.");
        }
        
        return ResponseEntity.ok(response);
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class PaymentRequest {
    private Long amount;
    private String productName;
    private String successUrl;
    private String cancelUrl;
}