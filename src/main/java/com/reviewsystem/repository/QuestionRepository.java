package com.reviewsystem.repository;
import com.reviewsystem.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByFormIdOrderByOrderIndex(Long formId);
    List<Question> findByFormIdAndType(Long formId, Question.QuestionType type);
}
