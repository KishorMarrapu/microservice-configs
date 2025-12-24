package com.OTRAS.DemoProject.Controller;
 
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.OTRAS.DemoProject.DTO.ExamAssignmentDTO;
import com.OTRAS.DemoProject.Service.ExamAssignmentService;

import lombok.RequiredArgsConstructor;
 
@RestController

@RequestMapping("/api/examAssignment")

@RequiredArgsConstructor

public class ExamAssignmentController {
 
    private final ExamAssignmentService examAssignmentService;
 

    @PostMapping("/assign")
    public ResponseEntity<String> assignSets(@RequestParam Long jobPostId) {
        String message = examAssignmentService.assignSetsSafely(jobPostId);
        return ResponseEntity.ok(message);
    }
    
    @GetMapping("/getAssignments")
    public ResponseEntity<List<ExamAssignmentDTO>> getAssignmentsByJobPostId(@RequestParam Long jobPostId) {
        List<ExamAssignmentDTO> assignments = examAssignmentService.getAssignmentsByJobPostId(jobPostId);
        return ResponseEntity.ok(assignments);
    }
    

    
    @PostMapping("/conductExam")
    public ResponseEntity<?> conductExam(
            @RequestParam String candidateName,
            @RequestParam String examRollNo) {

        try {
            Map<String, Object> response = examAssignmentService.conductExam(candidateName, examRollNo);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            //  Bad input or mismatch
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            //  Missing data or invalid state
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            //  Unexpected exception
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", " Something went wrong while starting the exam.",
                    "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/submitExam")
    public ResponseEntity<?> submitExam(
            @RequestParam String examRollNo,
            @RequestBody Map<String, String> submittedAnswers) {

        Map<String, Object> response = examAssignmentService.submitExam(examRollNo, submittedAnswers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<?> getStatusSummary() {
        return ResponseEntity.ok(examAssignmentService.getExamStatusSummary());
    }

    @GetMapping("/dashboard/details")
    public ResponseEntity<?> getStudentsByStatus(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(examAssignmentService.getStudentsByStatus(status));
    }

    @GetMapping("/dashboard/all")
    public ResponseEntity<?> getFullDashboard() {
        return ResponseEntity.ok(examAssignmentService.getFullDashboard());
    }

    
    @GetMapping("/getResultByRollNo")
    public ResponseEntity<?> getResultByRollNo(@RequestParam String examRollNo) {
        try {
            Map<String, Object> response = examAssignmentService.getResultByRollNo(examRollNo);
            if (response.containsKey("error")) {
                return ResponseEntity.status(404).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", " Something went wrong while fetching the result.",
                    "details", e.getMessage()
            ));
        }
    }
    
    
    @GetMapping("/resultsByCandidate")
    public ResponseEntity<?> getResultsByCandidate(@RequestParam Long candidateId) {
        try {
            List<Map<String, Object>> results = examAssignmentService.getResultsByCandidateId(candidateId);
            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", " Failed to fetch results.",
                "details", e.getMessage()
            ));
        }
    }

    
 // Add these to your ExamAssignmentController
    @GetMapping("/top-rankers")
    public ResponseEntity<?> getTopRankers(
            @RequestParam Long jobPostId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> topRankers = examAssignmentService.getTopRankers(jobPostId, limit);
            return ResponseEntity.ok(topRankers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/merit-list")
    public ResponseEntity<?> getFullMeritList(@RequestParam Long jobPostId) {
        try {
            List<Map<String, Object>> meritList = examAssignmentService.getFullMeritList(jobPostId);
            return ResponseEntity.ok(meritList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-rank")
    public ResponseEntity<?> getMyRank(@RequestParam String examRollNo) {
        try {
            Map<String, Object> rank = examAssignmentService.getMyRank(examRollNo);
            if (rank.containsKey("error")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/applications-by-otras")
    public ResponseEntity<?> getAllApplicationsByOtrasId(@RequestParam String otrasId) {
        try {
            List<Map<String, Object>> applications = examAssignmentService.getAllApplicationsByOtrasId(otrasId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/application-status")
    public ResponseEntity<?> getApplicationStatus(
            @RequestParam String otrasId, 
            @RequestParam Long jobPostId) {
        try {
            List<Map<String, Object>> timeline = examAssignmentService.getApplicationStatusByOtrasIdAndJobPost(otrasId, jobPostId);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/current-status")
    public ResponseEntity<?> getCurrentStatus(
            @RequestParam String otrasId, 
            @RequestParam Long jobPostId) {
        try {
            Map<String, Object> status = examAssignmentService.getCurrentApplicationStatus(otrasId, jobPostId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}

 