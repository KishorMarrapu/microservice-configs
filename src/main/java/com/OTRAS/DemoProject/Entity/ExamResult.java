//
//package com.OTRAS.DemoProject.Entity;
//
//import java.util.Map;
//
//import jakarta.persistence.ElementCollection;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.OneToOne;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ExamResult {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String examRollNo;
//    private String candidateName;
//    private String setName;
//    private Long questionPaperId;
//
//    private int totalQuestions;
//    private int correctAnswers;
//    private int wrongAnswers;
//    private double percentage;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assignment_id")
//    private ExamAssignment examAssignment;
//    private Double totalScore;  // 65.67 (after negative marking)  -- NEW FIELD
//
//    @ElementCollection  // JSON-like storage for categories
//    private Map<String, Integer> sectionCorrect;  // "GK":18, "Maths":15
//    private Map<String, Integer> sectionWrong;    // "GK":2, "Maths":5
//    
//}
//

package com.OTRAS.DemoProject.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examRollNo;
    private String candidateName;
    private String setName;
    private Long questionPaperId;

    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private double percentage;
    private Double totalScore=0.0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private ExamAssignment examAssignment;

    // ✅ FIXED: Use JSON String instead of Map
    @Column(columnDefinition = "TEXT")
    private String sectionCorrectJson;  // {"GK":18,"Maths":15}
    
    @Column(columnDefinition = "TEXT")
    private String sectionWrongJson;    // {"GK":2,"Maths":5}
}

