
package com.OTRAS.DemoProject.Entity;
 
import java.util.List;
 
import jakarta.persistence.CascadeType;

import jakarta.persistence.Entity;

import jakarta.persistence.FetchType;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;

import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;

import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Getter;

import lombok.NoArgsConstructor;

import lombok.Setter;
 
@Entity

@Getter

@Setter

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class QuestionSet {
 
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
 
    private String setName;
 
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "question_paper_id")

    private QuestionPaper questionPaper;
 
    @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<QuestionCategory> questionCategories;

}

 