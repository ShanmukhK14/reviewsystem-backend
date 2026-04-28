package com.reviewsystem.repository;
import com.reviewsystem.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByFormId(Long formId);
    List<Submission> findByStudentId(Long studentId);
    Optional<Submission> findByFormIdAndStudentId(Long formId, Long studentId);
    boolean existsByFormIdAndStudentId(Long formId, Long studentId);
    int countByFormId(Long formId);
}
