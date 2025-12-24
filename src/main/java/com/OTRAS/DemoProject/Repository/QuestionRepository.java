package com.OTRAS.DemoProject.Repository;
 
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
 
import com.OTRAS.DemoProject.Entity.Question;

import java.util.List;
 
@Repository

public interface QuestionRepository extends JpaRepository<Question, Long> {

//    List<Question> findByQuestionSetId(Long setId);
	List<Question> findByQuestionCategoryId(Long questionCategoryId);

}

 