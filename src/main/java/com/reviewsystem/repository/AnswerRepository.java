package com.reviewsystem.repository;
import com.reviewsystem.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findBySubmissionId(Long submissionId);
    List<Answer> findByQuestionId(Long questionId);
}
