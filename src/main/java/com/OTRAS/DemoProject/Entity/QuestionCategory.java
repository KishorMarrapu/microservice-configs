package com.OTRAS.DemoProject.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String questionCategoryName; // GK, Maths, Reasoning

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_set_id")
    private QuestionSet questionSet;

    @OneToMany(mappedBy = "questionCategory",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Question> questions;
}
