package com.OTRAS.DemoProject.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.OTRAS.DemoProject.DTO.JobCategoryDTO;
import com.OTRAS.DemoProject.DTO.JobPostDto;
import com.OTRAS.DemoProject.Service.JobPostService;

@RestController
@RequestMapping("/api/jobpost")
public class JobPostController {

    @Autowired
    private JobPostService jobPostService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<JobPostDto> createJobPost(

            @RequestParam String jobCategory,
            @RequestParam String jobTitle,
            @RequestParam int totalVacancy,
            @RequestParam String description,
            @RequestParam String postDate,
            @RequestParam String lastDate,
            @RequestParam String qualification,
            @RequestParam String gender,
            @RequestParam double fee,
            @RequestParam String importantDates,
            @RequestParam String interviewDates,
            @RequestParam String ageLimit,
            @RequestParam String additionalDetails,
            @RequestParam String examCenters,
            @RequestParam boolean livePhotoRequired,
            @RequestParam boolean signatureRequired,
            @RequestParam boolean declarationRequired,

            @RequestParam List<String> postName,
            @RequestParam List<Integer> total,
            @RequestParam List<Integer> age,

            @RequestParam List<String> religionName,
            @RequestParam List<String> seats,

            @RequestParam("dynamicFields") String dynamicFieldsJson,
            @RequestParam(required = false) MultipartFile uploadFile,
            @RequestParam("states") List<String> states,
            @RequestParam("country") String country
    ) {
        return ResponseEntity.ok(
                jobPostService.createJobPost(
                        jobCategory, jobTitle, totalVacancy, description, postDate, lastDate,
                        qualification, gender, fee, importantDates, interviewDates,
                        ageLimit, additionalDetails, examCenters,
                        livePhotoRequired, signatureRequired, declarationRequired,
                        postName, total, age, religionName, seats,  
                        dynamicFieldsJson, uploadFile, states, country
                )
        );
    }


    @GetMapping("/getAllJobNotifications")
    public ResponseEntity<List<JobPostDto>> getAllJobs() {
        List<JobPostDto> jobList = jobPostService.getAllJobPosts();
        return ResponseEntity.ok(jobList);
    }

   
    @GetMapping("/getJobNotificationById")
    public ResponseEntity<JobPostDto> getJobById(@RequestParam Long id) {
        JobPostDto job = jobPostService.getJobPostById(id);
        return ResponseEntity.ok(job);
    }

    
    @DeleteMapping("/deleteJobNotificationById")
    public ResponseEntity<String> deleteJob(@RequestParam Long id) {
        jobPostService.deleteJobPost(id);
        return ResponseEntity.ok("Job post deleted successfully with ID: " + id);
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String jobCategory,
            @RequestParam(required = false) String qualification
         
    ) {
        return ResponseEntity.ok(jobPostService.searchJobs(
                state, country, jobTitle, jobCategory, qualification
        ));
    }
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategoriesWithIds() {
        try {
            List<JobCategoryDTO> categories = jobPostService.getAllCategoriesWithIds();
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Something went wrong on the server"));
        }
    }

    
    
}
