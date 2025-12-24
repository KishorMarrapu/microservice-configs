package com.OTRAS.DemoProject.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.OTRAS.DemoProject.Entity.QuestionCategory;

@Repository
public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Long> {

    // Get all categories under a specific QuestionSet (Set A / Set B)
    List<QuestionCategory> findByQuestionSetId(Long questionSetId);
}
