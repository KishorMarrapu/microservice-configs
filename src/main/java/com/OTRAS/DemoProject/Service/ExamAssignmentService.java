package com.OTRAS.DemoProject.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.util.ArrayUtils;

import com.OTRAS.DemoProject.DTO.ExamAssignmentDTO;
import com.OTRAS.DemoProject.Entity.ApplicationStatus;
import com.OTRAS.DemoProject.Entity.Candidate;
import com.OTRAS.DemoProject.Entity.CandidateProfile;
import com.OTRAS.DemoProject.Entity.ExamAssignment;
import com.OTRAS.DemoProject.Entity.ExamResult;
import com.OTRAS.DemoProject.Entity.JobPost;
import com.OTRAS.DemoProject.Entity.PaymentSuccesfullData;
import com.OTRAS.DemoProject.Entity.Question;
import com.OTRAS.DemoProject.Entity.QuestionPaper;
import com.OTRAS.DemoProject.Entity.QuestionSet;
import com.OTRAS.DemoProject.Repository.ApplicationStatusRepository;
import com.OTRAS.DemoProject.Repository.ExamAssignmentRepository;
import com.OTRAS.DemoProject.Repository.ExamResultRepository;
import com.OTRAS.DemoProject.Repository.JobPostRepository;
import com.OTRAS.DemoProject.Repository.PaymentSuccesfullDataRepository;
import com.OTRAS.DemoProject.Repository.QuestionCategoryRepository;
import com.OTRAS.DemoProject.Repository.QuestionPaperRepository;
import com.OTRAS.DemoProject.Repository.QuestionSetRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamAssignmentService {

    private final QuestionPaperRepository questionPaperRepo;
    private final ExamAssignmentRepository examAssignmentRepo;
    private final QuestionSetRepository questionSetRepo;
    private final ExamResultRepository examResultRepo;
    private final QuestionCategoryRepository questionCategoryRepo;

    @Transactional
    public String assignSetsToCandidates(Long jobPostId) {
        List<PaymentSuccesfullData> students = paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");
        List<QuestionSet> sets = questionSetRepo.findByJobPostId(jobPostId);

        if (students.isEmpty()) return "No students found for JobPost ID: " + jobPostId;
        if (sets.isEmpty()) return "No question sets found for JobPost ID: " + jobPostId;

        int index = 0;
        int assignedCount = 0;

        for (PaymentSuccesfullData student : students) {
            List<ExamAssignment> existingAssignments = examAssignmentRepo.findAllByRollNumber(student.getExamRollNo());
            if (existingAssignments != null && !existingAssignments.isEmpty()) {
                continue;
            }

            QuestionSet selectedSet = sets.get(index % sets.size());

            ExamAssignment assignment = ExamAssignment.builder()
                    .rollNumber(student.getExamRollNo())
                    .jobPostId(jobPostId)
                    .setName(selectedSet.getSetName())
                    .questionPaperId(selectedSet.getQuestionPaper().getId())
                    .assigned(true)
                    .examStatus("Assigned")
                    .paymentSuccesfullData(student)
                    .build();

            examAssignmentRepo.save(assignment);
            assignedCount++;
            index++;
        }

        return "Assigned sets to " + assignedCount + " new students successfully.";
    }

    @Transactional(readOnly = true)
    public List<ExamAssignmentDTO> getAssignmentsByJobPostId(Long jobPostId) {
        return examAssignmentRepo.findByJobPostId(jobPostId)
                .stream()
                .map(assignment -> {
                    PaymentSuccesfullData payment = assignment.getPaymentSuccesfullData();
                    CandidateProfile profile = payment.getCandidateProfile();
                    Candidate candidate = (profile != null) ? profile.getCandidate() : null;

                    return new ExamAssignmentDTO(
                            assignment.getId(),
                            assignment.getRollNumber(),
                            assignment.getJobPostId(),
                            assignment.getSetName(),
                            assignment.getQuestionPaperId(),
                            assignment.isAssigned(),
                            payment.getOtrId(),
                            payment.getPaymentStatus(),
                            payment.getExamRollNo(),
                            candidate != null ? candidate.getFullName() : null,
                            candidate != null ? candidate.getDateOfBirth().toString() : null,
                            candidate != null ? candidate.getGender() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> conductExam(String candidateName, String examRollNo) {
        // Step 1: Validate assignment exists
        ExamAssignment assignment = examAssignmentRepo.findAllByRollNumber(examRollNo)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("❌ No assignment found for the given roll number."));

        // Step 2: Validate payment and candidate
        PaymentSuccesfullData payment = assignment.getPaymentSuccesfullData();
        if (payment == null) {
            throw new IllegalStateException("❌ Payment record is missing for this assignment.");
        }

        CandidateProfile profile = payment.getCandidateProfile();
        if (profile == null) {
            throw new IllegalStateException("❌ Candidate profile not found for this payment record.");
        }

        String dbName = profile.getCandidateName() != null
                ? profile.getCandidateName()
                : (profile.getCandidate() != null ? profile.getCandidate().getFullName() : null);

        if (dbName == null || !dbName.equalsIgnoreCase(candidateName)) {
            throw new IllegalArgumentException("❌ Candidate name does not match our records.");
        }

        // Step 3: Check if already completed
        ExamResult existingResult = examResultRepo.findByExamRollNo(examRollNo);
        if (existingResult != null) {
            Map<String, Object> restricted = new LinkedHashMap<>();
            restricted.put("examRollNo", examRollNo);
            restricted.put("candidateName", existingResult.getCandidateName());
            restricted.put("message", "⚠️ You have already completed the exam. Multiple attempts not allowed.");
            restricted.put("totalQuestions", existingResult.getTotalQuestions());
            restricted.put("correctAnswers", existingResult.getCorrectAnswers());
            restricted.put("wrongAnswers", existingResult.getWrongAnswers());
            restricted.put("totalScore", String.format("%.2f", existingResult.getTotalScore()));
            restricted.put("percentage", existingResult.getPercentage() + "%");
            return restricted;
        }

        // Step 4: Load question paper and set (NEW HIERARCHY)
        QuestionPaper questionPaper = questionPaperRepo.findById(assignment.getQuestionPaperId())
                .orElseThrow(() -> new IllegalArgumentException("❌ Question paper not found."));

        QuestionSet questionSet = questionSetRepo.findByQuestionPaperIdAndSetName(
                questionPaper.getId(), assignment.getSetName());

        if (questionSet == null || questionSet.getQuestionCategories() == null || 
            questionSet.getQuestionCategories().isEmpty()) {
            throw new IllegalStateException("❌ No categories found for this set.");
        }

        // Step 5: Get ALL questions from ALL categories + RANDOMIZE
        List<Question> allQuestions = questionSet.getQuestionCategories().stream()
                .flatMap(category -> category.getQuestions().stream())
                .collect(Collectors.toList());

        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("❌ No questions found in categories.");
        }

        Collections.shuffle(allQuestions); // ✅ RANDOMIZATION for each candidate

        // Step 6: Mark exam as started
        if (!"Submitted".equalsIgnoreCase(assignment.getExamStatus())) {
            assignment.setExamStatus("Started");
            examAssignmentRepo.save(assignment);
        }

        // Step 7: Build response with randomized questions + category info
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("candidateName", dbName);
        response.put("examRollNo", examRollNo);
        response.put("setName", questionSet.getSetName());
        response.put("paperName", questionPaper.getPaperName());
        response.put("paperId", questionPaper.getId());
        response.put("totalQuestions", allQuestions.size());

        List<Map<String, Object>> questionList = allQuestions.stream().map(q -> {
            Map<String, Object> questionMap = new LinkedHashMap<>();
            questionMap.put("id", q.getId());
            questionMap.put("category", q.getQuestionCategory().getQuestionCategoryName());
            questionMap.put("questionText", q.getQuestionText());
            questionMap.put("optionA", q.getOptionA());
            questionMap.put("optionB", q.getOptionB());
            questionMap.put("optionC", q.getOptionC());
            questionMap.put("optionD", q.getOptionD());
            questionMap.put("marks", q.getMarks() != null ? q.getMarks() : 1.0);
            questionMap.put("negativeMarks", q.getNegativeMarks() != null ? q.getNegativeMarks() : 0.33);
            return questionMap;
        }).collect(Collectors.toList());

        response.put("questions", questionList);
        response.put("message", "✅ Exam started with randomized questions from all categories.");
        return response;
    }

//    @Transactional
//    public Map<String, Object> submitExam(String examRollNo, Map<String, String> submittedAnswers) {
//        ExamResult existingResult = examResultRepo.findByExamRollNo(examRollNo);
//        if (existingResult != null) {
//            Map<String, Object> duplicateResponse = new LinkedHashMap<>();
//            duplicateResponse.put("examRollNo", examRollNo);
//            duplicateResponse.put("candidateName", existingResult.getCandidateName());
//            duplicateResponse.put("message", "Exam already submitted. Duplicate submission not allowed.");
//            duplicateResponse.put("totalQuestions", existingResult.getTotalQuestions());
//            duplicateResponse.put("correctAnswers", existingResult.getCorrectAnswers());
//            duplicateResponse.put("wrongAnswers", existingResult.getWrongAnswers());
//            duplicateResponse.put("totalScore", String.format("%.2f", existingResult.getTotalScore()));
//            duplicateResponse.put("percentage", existingResult.getPercentage() + "%");
//            return duplicateResponse;
//        }
//
//        Optional<ExamAssignment> optionalAssignment = examAssignmentRepo.findByRollNumber(examRollNo);
//        if (optionalAssignment.isEmpty()) return Map.of("error", "Invalid exam roll number");
//
//        ExamAssignment assignment = optionalAssignment.get();
//        QuestionPaper questionPaper = questionPaperRepo.findById(assignment.getQuestionPaperId()).orElse(null);
//        if (questionPaper == null) return Map.of("error", "Question paper not found");
//
//        QuestionSet questionSet = questionSetRepo.findByQuestionPaperIdAndSetName(
//                questionPaper.getId(), assignment.getSetName());
//        if (questionSet == null || questionSet.getQuestionCategories() == null) {
//            return Map.of("error", "No questions found for assigned set");
//        }
//
//        // ✅ Get questions from NEW hierarchy (Set → Categories → Questions)
//        List<Question> allQuestions = questionSet.getQuestionCategories().stream()
//                .flatMap(category -> category.getQuestions().stream())
//                .collect(Collectors.toList());
//
//        // ✅ SECTION-WISE SCORING + NEGATIVE MARKING
//        Map<String, List<Question>> questionsByCategory = allQuestions.stream()
//                .collect(Collectors.groupingBy(q -> q.getQuestionCategory().getQuestionCategoryName()));
//
//        Map<String, Integer> sectionCorrect = new HashMap<>();
//        Map<String, Integer> sectionWrong = new HashMap<>();
//        double totalScore = 0.0;
//        int totalCorrect = 0;
//        int totalWrong = 0;
//
//        // Calculate section-wise scores
//        for (Map.Entry<String, List<Question>> entry : questionsByCategory.entrySet()) {
//            String category = entry.getKey();
//            List<Question> categoryQuestions = entry.getValue();
//            
//            int correctInCategory = 0;
//            int wrongInCategory = 0;
//            
//            for (Question q : categoryQuestions) {
//                String selected = submittedAnswers.get(String.valueOf(q.getId()));
//                String correctAnswer = q.getCorrectAnswer();
//                
//                if (selected != null && selected.equalsIgnoreCase(correctAnswer)) {
//                    // Correct answer: +marks
//                    totalScore += (q.getMarks() != null ? q.getMarks() : 1.0);
//                    correctInCategory++;
//                    totalCorrect++;
//                } else if (selected != null) {
//                    // Wrong answer: -negativeMarks
//                    totalScore -= (q.getNegativeMarks() != null ? q.getNegativeMarks() : 0.33);
//                    wrongInCategory++;
//                    totalWrong++;
//                }
//                // Unanswered = 0 (no penalty)
//            }
//            
//            sectionCorrect.put(category, correctInCategory);
//            sectionWrong.put(category, wrongInCategory);
//        }
//
//        int totalQuestions = allQuestions.size();
//        double percentage = (totalQuestions > 0) ? (totalScore / (totalQuestions * 1.0)) * 100 : 0;
//
//        ExamResult result = ExamResult.builder()
//                .examRollNo(examRollNo)
//                .candidateName(assignment.getPaymentSuccesfullData().getCandidateProfile().getCandidate().getFullName())
//                .setName(assignment.getSetName())
//                .questionPaperId(questionPaper.getId())
//                .totalQuestions(totalQuestions)
//                .correctAnswers(totalCorrect)
//                .wrongAnswers(totalWrong)
//                .totalScore(totalScore)
//                .percentage(percentage)
//                .sectionCorrect(sectionCorrect)
//                .sectionWrong(sectionWrong)
//                .examAssignment(assignment)
//                .build();
//        
//        examResultRepo.save(result);
//        assignment.setExamStatus("Submitted");
//        examAssignmentRepo.save(assignment);
//
//        // Enhanced response with section breakdown
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("examRollNo", examRollNo);
//        response.put("candidateName", result.getCandidateName());
//        response.put("setName", result.getSetName());
//        response.put("totalQuestions", totalQuestions);
//        response.put("correctAnswers", totalCorrect);
//        response.put("wrongAnswers", totalWrong);
//        response.put("totalScore", String.format("%.2f", totalScore));
//        response.put("percentage", String.format("%.2f%%", percentage));
//        
//        // Section-wise breakdown
//        Map<String, Object> sectionBreakdown = new LinkedHashMap<>();
//        for (String category : sectionCorrect.keySet()) {
//            int correct = sectionCorrect.get(category);
//            int wrong = sectionWrong.getOrDefault(category, 0);
//            int totalInSection = correct + wrong;
//            sectionBreakdown.put(category, String.format("%d/%d", correct, totalInSection));
//        }
//        response.put("sectionScores", sectionBreakdown);
//        response.put("message", "✅ Exam submitted with negative marking & section-wise scoring");
//        
//        return response;
//    }
    @Transactional
    public Map<String, Object> submitExam(String examRollNo, Map<String, String> submittedAnswers) {
        ExamResult existingResult = examResultRepo.findByExamRollNo(examRollNo);
        if (existingResult != null) {
            Map<String, Object> duplicateResponse = new LinkedHashMap<>();
            duplicateResponse.put("examRollNo", examRollNo);
            duplicateResponse.put("candidateName", existingResult.getCandidateName());
            duplicateResponse.put("message", "Exam already submitted. Duplicate submission not allowed.");
            duplicateResponse.put("totalQuestions", existingResult.getTotalQuestions());
            duplicateResponse.put("correctAnswers", existingResult.getCorrectAnswers());
            duplicateResponse.put("wrongAnswers", existingResult.getWrongAnswers());
            duplicateResponse.put("totalScore", String.format("%.2f", existingResult.getTotalScore()));
            duplicateResponse.put("percentage", existingResult.getPercentage() + "%");
            return duplicateResponse;
        }

        Optional<ExamAssignment> optionalAssignment = examAssignmentRepo.findByRollNumber(examRollNo);
        if (optionalAssignment.isEmpty()) return Map.of("error", "Invalid exam roll number");

        ExamAssignment assignment = optionalAssignment.get();
        QuestionPaper questionPaper = questionPaperRepo.findById(assignment.getQuestionPaperId()).orElse(null);
        if (questionPaper == null) return Map.of("error", "Question paper not found");

        QuestionSet questionSet = questionSetRepo.findByQuestionPaperIdAndSetName(
                questionPaper.getId(), assignment.getSetName());
        if (questionSet == null || questionSet.getQuestionCategories() == null) {
            return Map.of("error", "No questions found for assigned set");
        }

        // ✅ Get questions from NEW hierarchy (Set → Categories → Questions)
        List<Question> allQuestions = questionSet.getQuestionCategories().stream()
                .flatMap(category -> category.getQuestions().stream())
                .collect(Collectors.toList());

        // ✅ SECTION-WISE SCORING + NEGATIVE MARKING
        Map<String, List<Question>> questionsByCategory = allQuestions.stream()
                .collect(Collectors.groupingBy(q -> q.getQuestionCategory().getQuestionCategoryName()));

        Map<String, Integer> sectionCorrect = new HashMap<>();
        Map<String, Integer> sectionWrong = new HashMap<>();
        double totalScore = 0.0;
        int totalCorrect = 0;
        int totalWrong = 0;

        // Calculate section-wise scores
        for (Map.Entry<String, List<Question>> entry : questionsByCategory.entrySet()) {
            String category = entry.getKey();
            List<Question> categoryQuestions = entry.getValue();
            
            int correctInCategory = 0;
            int wrongInCategory = 0;
            
            for (Question q : categoryQuestions) {
                String selected = submittedAnswers.get(String.valueOf(q.getId()));
                String correctAnswer = q.getCorrectAnswer();
                
                if (selected != null && selected.equalsIgnoreCase(correctAnswer)) {
                    // Correct answer: +marks
                    totalScore += (q.getMarks() != null ? q.getMarks() : 1.0);
                    correctInCategory++;
                    totalCorrect++;
                } else if (selected != null) {
                    // Wrong answer: -negativeMarks
                    totalScore -= (q.getNegativeMarks() != null ? q.getNegativeMarks() : 0.33);
                    wrongInCategory++;
                    totalWrong++;
                }
                // Unanswered = 0 (no penalty)
            }
            
            sectionCorrect.put(category, correctInCategory);
            sectionWrong.put(category, wrongInCategory);
        }

        int totalQuestions = allQuestions.size();
        double percentage = (totalQuestions > 0) ? (totalScore / (totalQuestions * 1.0)) * 100 : 0;

        // ✅ FIXED: Create result WITHOUT Map fields
        ExamResult result = ExamResult.builder()
                .examRollNo(examRollNo)
                .candidateName(assignment.getPaymentSuccesfullData().getCandidateProfile().getCandidate().getFullName())
                .setName(assignment.getSetName())
                .questionPaperId(questionPaper.getId())
                .totalQuestions(totalQuestions)
                .correctAnswers(totalCorrect)
                .wrongAnswers(totalWrong)
                .totalScore(totalScore)
                .percentage(percentage)
                .examAssignment(assignment)
                .build();

        // ✅ FIXED: Convert Maps to JSON strings
        ObjectMapper mapper = new ObjectMapper();
        try {
            result.setSectionCorrectJson(mapper.writeValueAsString(sectionCorrect));
            result.setSectionWrongJson(mapper.writeValueAsString(sectionWrong));
        } catch (Exception e) {
            result.setSectionCorrectJson("{}");
            result.setSectionWrongJson("{}");
        }
        
        examResultRepo.save(result);
        assignment.setExamStatus("Submitted");
        examAssignmentRepo.save(assignment);

        // Enhanced response with section breakdown
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("examRollNo", examRollNo);
        response.put("candidateName", result.getCandidateName());
        response.put("setName", result.getSetName());
        response.put("totalQuestions", totalQuestions);
        response.put("correctAnswers", totalCorrect);
        response.put("wrongAnswers", totalWrong);
        response.put("totalScore", String.format("%.2f", totalScore));
        response.put("percentage", String.format("%.2f%%", percentage));
        
        // ✅ Section-wise breakdown (from in-memory maps)
        Map<String, Object> sectionBreakdown = new LinkedHashMap<>();
        for (String category : sectionCorrect.keySet()) {
            int correct = sectionCorrect.get(category);
            int wrong = sectionWrong.getOrDefault(category, 0);
            int totalInSection = correct + wrong;
            sectionBreakdown.put(category, String.format("%d/%d", correct, totalInSection));
        }
        response.put("sectionScores", sectionBreakdown);
        response.put("message", "✅ Exam submitted with negative marking & section-wise scoring");
        
        return response;
    }


//    @Transactional(readOnly = true)
//    public Map<String, Object> getResultByRollNo(String examRollNo) {
//        ExamResult result = examResultRepo.findByExamRollNo(examRollNo);
//        if (result == null) {
//            return Map.of("error", "❌ No result found for the given roll number: " + examRollNo);
//        }
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("examRollNo", result.getExamRollNo());
//        response.put("candidateName", result.getCandidateName());
//        response.put("setName", result.getSetName());
//        response.put("questionPaperId", result.getQuestionPaperId());
//        response.put("totalQuestions", result.getTotalQuestions());
//        response.put("correctAnswers", result.getCorrectAnswers());
//        response.put("wrongAnswers", result.getWrongAnswers());
//        response.put("totalScore", String.format("%.2f", result.getTotalScore()));
//        response.put("percentage", result.getPercentage() + "%");
//        response.put("status", "✅ Result found");
//
//        // Section-wise breakdown
//        Map<String, Object> sectionBreakdown = new LinkedHashMap<>();
//        if (result.getSectionCorrect() != null) {
//            for (String category : result.getSectionCorrect().keySet()) {
//                int correct = result.getSectionCorrect().get(category);
//                int wrong = result.getSectionWrong() != null ? result.getSectionWrong().getOrDefault(category, 0) : 0;
//                int totalInSection = correct + wrong;
//                sectionBreakdown.put(category, String.format("%d/%d", correct, totalInSection));
//            }
//        }
//        response.put("sectionScores", sectionBreakdown);
//
//        return response;
//    }
    @Transactional(readOnly = true)
    public Map<String, Object> getResultByRollNo(String examRollNo) {
        ExamResult result = examResultRepo.findByExamRollNo(examRollNo);
        if (result == null) {
            return Map.of("error", "❌ No result found for the given roll number: " + examRollNo);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("examRollNo", result.getExamRollNo());
        response.put("candidateName", result.getCandidateName());
        response.put("setName", result.getSetName());
        response.put("questionPaperId", result.getQuestionPaperId());
        response.put("totalQuestions", result.getTotalQuestions());
        response.put("correctAnswers", result.getCorrectAnswers());
        response.put("wrongAnswers", result.getWrongAnswers());
//        response.put("totalScore", String.format("%.2f", result.getTotalScore()));
        response.put("totalScore", result.getTotalScore() != null ? 
        	    String.format("%.2f", result.getTotalScore()) : "0.00");
        response.put("percentage", result.getPercentage() + "%");
        response.put("status", "✅ Result found");

        // ✅ FIXED: Section-wise breakdown from JSON strings
        Map<String, Object> sectionBreakdown = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            // Parse sectionCorrectJson
            if (result.getSectionCorrectJson() != null && !result.getSectionCorrectJson().isEmpty() && !"{}".equals(result.getSectionCorrectJson())) {
                Map<String, Integer> sectionCorrect = mapper.readValue(
                    result.getSectionCorrectJson(), new TypeReference<Map<String, Integer>>() {});
                
                // Parse sectionWrongJson (optional)
                Map<String, Integer> sectionWrong = new HashMap<>();
                if (result.getSectionWrongJson() != null && !result.getSectionWrongJson().isEmpty() && !"{}".equals(result.getSectionWrongJson())) {
                    sectionWrong = mapper.readValue(
                        result.getSectionWrongJson(), new TypeReference<Map<String, Integer>>() {});
                }
                
                // Build section breakdown
                for (String category : sectionCorrect.keySet()) {
                    int correct = sectionCorrect.get(category);
                    int wrong = sectionWrong.getOrDefault(category, 0);
                    int totalInSection = correct + wrong;
                    sectionBreakdown.put(category, String.format("%d/%d", correct, totalInSection));
                }
            }
        } catch (Exception e) {
            sectionBreakdown.put("error", "Section data unavailable");
        }
        
        response.put("sectionScores", sectionBreakdown);
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getResultsByCandidateId(Long candidateId) {
        List<PaymentSuccesfullData> payments = paymentRepo.findByCandidateProfile_Candidate_Id(candidateId);
        if (payments.isEmpty()) {
            throw new IllegalArgumentException("❌ No payment records found for this candidate.");
        }

        List<ExamAssignment> assignments = examAssignmentRepo.findByPaymentSuccesfullDataIn(payments);
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("❌ No exam assignments found for this candidate.");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (ExamAssignment assignment : assignments) {
            String rollNo = assignment.getRollNumber();
            ExamResult result = examResultRepo.findByExamRollNo(rollNo);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("examRollNo", rollNo);
            data.put("candidateName", assignment.getPaymentSuccesfullData().getCandidateProfile().getCandidateName());
            data.put("setName", assignment.getSetName());
            data.put("questionPaperId", assignment.getQuestionPaperId());

            if (result != null) {
                data.put("totalQuestions", result.getTotalQuestions());
                data.put("correctAnswers", result.getCorrectAnswers());
                data.put("wrongAnswers", result.getWrongAnswers());
                data.put("totalScore", String.format("%.2f", result.getTotalScore()));
                data.put("percentage", result.getPercentage() + "%");
                data.put("status", "✅ Result found");
            } else {
                data.put("totalQuestions", "-");
                data.put("correctAnswers", "-");
                data.put("wrongAnswers", "-");
                data.put("totalScore", "-");
                data.put("percentage", "-");
                data.put("status", "❌ Result not yet published");
            }

            results.add(data);
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDashboardData() {
        List<ExamAssignment> assignments = examAssignmentRepo.findAll();
        return assignments.stream().map(a -> {
            CandidateProfile profile = a.getPaymentSuccesfullData().getCandidateProfile();
            String candidateName = profile != null ? profile.getCandidate().getFullName() : "N/A";
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("candidateName", candidateName);
            row.put("examRollNo", a.getRollNumber());
            row.put("setName", a.getSetName());
            row.put("jobPostId", a.getJobPostId());
            row.put("examStatus", a.getExamStatus());
            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExamStatusSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        List<ExamAssignment> all = examAssignmentRepo.findAll();

        long assignedCount = all.stream().filter(a -> "Assigned".equalsIgnoreCase(a.getExamStatus())).count();
        long startedCount = all.stream().filter(a -> "Started".equalsIgnoreCase(a.getExamStatus())).count();
        long submittedCount = all.stream().filter(a -> "Submitted".equalsIgnoreCase(a.getExamStatus())).count();
        long paymentCount = all.stream().filter(a -> "SUCCESS".equalsIgnoreCase(a.getPaymentSuccesfullData().getPaymentStatus())).count();

        summary.put("Payment Successful", paymentCount);
        summary.put("Assigned", assignedCount);
        summary.put("Started", startedCount);
        summary.put("Submitted", submittedCount);
        summary.put("Total Students", all.size());
        return summary;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentsByStatus(String status) {
        List<ExamAssignment> list;
        if (status == null || status.isEmpty() || "All".equalsIgnoreCase(status)) {
            list = examAssignmentRepo.findAll();
        } else {
            list = examAssignmentRepo.findAll().stream()
                    .filter(a -> status.equalsIgnoreCase(a.getExamStatus()))
                    .collect(Collectors.toList());
        }

        return list.stream().map(a -> {
            CandidateProfile profile = a.getPaymentSuccesfullData().getCandidateProfile();
            String candidateName = profile != null ? profile.getCandidate().getFullName() : "N/A";
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("candidateName", candidateName);
            row.put("examRollNo", a.getRollNumber());
            row.put("setName", a.getSetName());
            row.put("jobPostId", a.getJobPostId());
            row.put("examStatus", a.getExamStatus());
            row.put("paymentStatus", a.getPaymentSuccesfullData().getPaymentStatus());
            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFullDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("summary", getExamStatusSummary());
        dashboard.put("students", getStudentsByStatus("All"));
        return dashboard;
    }

    @Transactional
    public String assignSetsSafely(Long jobPostId) {
        List<PaymentSuccesfullData> students = paymentRepo.findAllByJobPostIdAndPaymentStatus(jobPostId, "SUCCESS");
        List<QuestionSet> sets = questionSetRepo.findByJobPostId(jobPostId);

        if (students.isEmpty()) {
            return "❌ No successful payments found for JobPost ID: " + jobPostId;
        }
        if (sets.isEmpty()) {
            return "❌ No question sets found for JobPost ID: " + jobPostId;
        }

        int index = 0;
        int assignedCount = 0;
        int skippedCount = 0;

        for (PaymentSuccesfullData student : students) {
            String rollNo = student.getExamRollNo();
            List<ExamAssignment> existingByRoll = examAssignmentRepo.findAllByRollNumber(rollNo);
            if (!existingByRoll.isEmpty()) {
                skippedCount++;
                continue;
            }

            List<ExamAssignment> existingByPayment = examAssignmentRepo.findByPaymentSuccesfullData(student);
            if (!existingByPayment.isEmpty()) {
                skippedCount++;
                continue;
            }

            QuestionSet selectedSet = sets.get(index % sets.size());
            ExamAssignment newAssignment = ExamAssignment.builder()
                    .rollNumber(rollNo)
                    .jobPostId(jobPostId)
                    .setName(selectedSet.getSetName())
                    .questionPaperId(selectedSet.getQuestionPaper().getId())
                    .assigned(true)
                    .examStatus("Assigned")
                    .paymentSuccesfullData(student)
                    .build();

            examAssignmentRepo.save(newAssignment);
            assignedCount++;
            index++;
        }

        return String.format("✅ %d students assigned successfully, %d skipped (already assigned).", assignedCount, skippedCount);
    }
    
    
	 @Transactional(readOnly = true)
	 public List<Map<String, Object>> getTopRankers(Long jobPostId, int limit) {
		    List<ExamResult> results = examResultRepo.findByQuestionPaperIdOrderByTotalScoreDesc(jobPostId);
		    
		    return results.stream()
		        .limit(limit)
		        .map(result -> {
		            Map<String, Object> ranker = new LinkedHashMap<>();
		            ranker.put("rank", results.indexOf(result) + 1);
		            ranker.put("examRollNo", result.getExamRollNo());
		            ranker.put("candidateName", result.getCandidateName());
		            
		            //   Null-safe totalScore
		            Double score = result.getTotalScore();
		            ranker.put("totalScore", score != null ? String.format("%.2f", score) : "0.00");
		            
		            //  Null-safe percentage
		            Double perc = result.getPercentage();
		            ranker.put("percentage", perc != null ? String.format("%.2f%%", perc) : "0.00%");
		            
		            return ranker;
		        })
		        .collect(Collectors.toList());
		}


    // ✅ RANK 2: Get Full Merit List (All Candidates Ranked)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFullMeritList(Long jobPostId) {
        List<ExamResult> results = examResultRepo.findByQuestionPaperIdOrderByTotalScoreDesc(jobPostId);
        
        return IntStream.range(0, results.size())
            .mapToObj(i -> {
                ExamResult result = results.get(i);
                Map<String, Object> merit = new LinkedHashMap<>();
                merit.put("rank", i + 1);
                merit.put("examRollNo", result.getExamRollNo());
                merit.put("candidateName", result.getCandidateName());
                merit.put("setName", result.getSetName());
                merit.put("totalScore", result.getTotalScore() != null ? 
                	    String.format("%.2f", result.getTotalScore()) : "0.00");  
                merit.put("percentage", String.format("%.2f%%", result.getPercentage()));
                return merit;
            })
            .collect(Collectors.toList());
    }

    // ✅ RANK 3: Get Candidate Rank (Where am I?)
    @Transactional(readOnly = true)
    public Map<String, Object> getMyRank(String examRollNo) {
        ExamResult result = examResultRepo.findByExamRollNo(examRollNo);
        if (result == null) {
            return Map.of("error", "No result found");
        }
        
        List<ExamResult> allResults = examResultRepo.findByQuestionPaperIdOrderByTotalScoreDesc(result.getQuestionPaperId());
        int rank = allResults.indexOf(result) + 1;
        
        Map<String, Object> myRank = new LinkedHashMap<>();
        myRank.put("examRollNo", examRollNo);
        myRank.put("candidateName", result.getCandidateName());
        myRank.put("rank", rank);
        myRank.put("totalScore", result.getTotalScore() != null ? 
        	    String.format("%.2f", result.getTotalScore()) : "0.00");
        myRank.put("totalCandidates", allResults.size());
        myRank.put("message", String.format("✅ Your rank is %d out of %d", rank, allResults.size()));
        
        return myRank;
    }
    
    @Autowired
    private ApplicationStatusRepository statusRepo;

    @Autowired
    private JobPostRepository jobPostRepo;

    @Autowired
    private PaymentSuccesfullDataRepository paymentRepo;

 // FIXED: Use custom SINGLE method for status tracking
    private Long getCandidateIdByOtrasId(String otrasId) {  
        PaymentSuccesfullData payment = paymentRepo.findByOtrIdForStatus(otrasId);
        if (payment == null || payment.getCandidateProfile() == null || payment.getCandidateProfile().getCandidate() == null) {
            return null;
        }
        return payment.getCandidateProfile().getCandidate().getId();
    }


    private String getJobPostTitle(Long jobPostId) {
        return jobPostRepo.findById(jobPostId)
            .map(JobPost::getJobTitle)
            .orElse("Unknown Job");
    }

    // MAIN API 1: ALL applications by OTRAS-ID
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllApplicationsByOtrasId(String otrasId) {
        List<ApplicationStatus> statuses = statusRepo.findByOtrasIdOrderByStatusDateTimeDesc(otrasId);
        
        return statuses.stream()
            .collect(Collectors.groupingBy(ApplicationStatus::getJobPostId))
            .entrySet().stream()
            .map(entry -> {
                Long jobPostId = entry.getKey();
                ApplicationStatus latest = entry.getValue().get(0);
                
                Map<String, Object> app = new LinkedHashMap<>();
                app.put("jobPostId", jobPostId);
                app.put("jobPostTitle", getJobPostTitle(jobPostId));
                app.put("currentStatus", latest.getStatus());
                app.put("examRollNo", latest.getExamRollNo());
                app.put("lastUpdated", latest.getStatusDateTime().toString());
                return app;
            })
            .sorted((a, b) -> {
                String statusA = (String) a.get("currentStatus");
                String statusB = (String) b.get("currentStatus");
                return compareStatus(statusB, statusA); // Higher status first
            })
            .collect(Collectors.toList());
    }

    // MAIN API 2: Specific job timeline
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getApplicationStatusByOtrasIdAndJobPost(String otrasId, Long jobPostId) {
        return statusRepo.findByOtrasIdAndJobPostIdOrderByStatusDateTimeDesc(otrasId, jobPostId)
            .stream()
            .map(status -> {
                Map<String, Object> timeline = new LinkedHashMap<>();
                timeline.put("status", status.getStatus());
                timeline.put("statusDateTime", status.getStatusDateTime().toString());
                timeline.put("message", status.getStatusMessage() != null ? status.getStatusMessage() : "");
                timeline.put("examRollNo", status.getExamRollNo());
                return timeline;
            })
            .collect(Collectors.toList());
    }

    // Current status summary
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentApplicationStatus(String otrasId, Long jobPostId) {
        List<ApplicationStatus> statuses = statusRepo.findByOtrasIdAndJobPostIdOrderByStatusDateTimeDesc(otrasId, jobPostId);
        ApplicationStatus latest = statuses.isEmpty() ? null : statuses.get(0);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("otrasId", otrasId);
        response.put("jobPostId", jobPostId);
        response.put("currentStatus", latest != null ? latest.getStatus() : "NOT_APPLIED");
        response.put("examRollNo", latest != null ? latest.getExamRollNo() : null);
        response.put("lastUpdated", latest != null ? latest.getStatusDateTime().toString() : "Never");
        response.put("nextAction", getNextAction(latest != null ? latest.getStatus() : null));
        
        return response;
    }

    private String getNextAction(String currentStatus) {
        switch (currentStatus != null ? currentStatus : "") {
            case "NOT_APPLIED": return "Apply for job";
            case "APPLIED": return "Complete payment";
            case "PAYMENT_SUCCESS": return "Wait for exam assignment";
            case "EXAM_ASSIGNED": return "Download admit card & start exam";
            case "EXAM_STARTED": return "Complete your exam";
            case "SUBMITTED": return "Wait for result publishing";
            case "RESULT_PUBLISHED": return "Check your result & rank";
            default: return "Contact support";
        }
    }

    private int compareStatus(String status1, String status2) {
        String[] order = {"RESULT_PUBLISHED", "SUBMITTED", "EXAM_STARTED", "EXAM_ASSIGNED", "PAYMENT_SUCCESS", "APPLIED"};
        
        int index1 = getStatusIndex(status1, order);
        int index2 = getStatusIndex(status2, order);
        
        return Integer.compare(index2, index1); // Higher priority first
    }

    private int getStatusIndex(String status, String[] order) {
        if (status == null) return order.length; // Lowest priority for null
        
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(status)) {
                return i;
            }
        }
        return order.length; // Lowest priority for unknown status
    }



    @Transactional
    public void updateApplicationStatus(String otrasId, Long jobPostId, String status, String message) {
        ApplicationStatus appStatus = ApplicationStatus.builder()
            .otrasId(otrasId)
            .jobPostId(jobPostId)
            .candidateId(getCandidateIdByOtrasId(otrasId))
            .status(status)
            .statusMessage(message)
            .build();
        statusRepo.save(appStatus);
    }


}
