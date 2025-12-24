package com.OTRAS.DemoProject.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.OTRAS.DemoProject.Entity.ExamResult;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

	ExamResult findByExamRollNo(String examRollNo);
    //Optional<ExamResult> findByExamIdAndOtrId(Long examId, String otrId);
    //List<ExamResult> findByOtrId(String otrId);

	// Add to ExamResultRepository interface
	List<ExamResult> findByQuestionPaperIdOrderByTotalScoreDesc(Long questionPaperId);
}


