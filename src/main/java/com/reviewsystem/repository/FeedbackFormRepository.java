package com.reviewsystem.repository;
import com.reviewsystem.model.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {
    List<FeedbackForm> findByStatus(FeedbackForm.Status status);
    List<FeedbackForm> findByAdminId(Long adminId);
}
