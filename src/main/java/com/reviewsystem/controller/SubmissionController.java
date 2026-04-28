package com.reviewsystem.controller;

import com.reviewsystem.model.*;
import com.reviewsystem.repository.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private FeedbackFormRepository formRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private AnswerRepository answerRepo;

    // POST /api/submissions — submit feedback
    @PostMapping
    public ResponseEntity<?> submit(@RequestBody SubmitRequest req) {
        if (submissionRepo.existsByFormIdAndStudentId(req.getFormId(), req.getStudentId()))
            return ResponseEntity.badRequest()
                .body(Map.of("error", "You have already submitted feedback for this form"));

        FeedbackForm form = formRepo.findById(req.getFormId())
            .orElseThrow(() -> new RuntimeException("Form not found"));
        User student = userRepo.findById(req.getStudentId())
            .orElseThrow(() -> new RuntimeException("Student not found"));

        Submission sub = submissionRepo.save(Submission.builder()
            .form(form).student(student)
            .submittedAt(LocalDateTime.now()).build());

        if (req.getAnswers() != null) {
            req.getAnswers().forEach((questionId, answerValue) -> {
                questionRepo.findById(questionId).ifPresent(q -> {
                    answerRepo.save(Answer.builder()
                        .submission(sub).question(q)
                        .answerValue(String.valueOf(answerValue)).build());
                });
            });
        }

        return ResponseEntity.ok(Map.of(
            "message", "Feedback submitted successfully!",
            "submissionId", sub.getId()
        ));
    }

    // GET /api/submissions/check?formId=1&studentId=2
    @GetMapping("/check")
    public ResponseEntity<?> check(@RequestParam Long formId, @RequestParam Long studentId) {
        return ResponseEntity.ok(Map.of(
            "submitted", submissionRepo.existsByFormIdAndStudentId(formId, studentId)
        ));
    }

    // GET /api/submissions/student/{studentId}
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> byStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(
            submissionRepo.findByStudentId(studentId).stream().map(s -> Map.of(
                "submissionId", s.getId(),
                "formId", s.getForm().getId(),
                "formTitle", s.getForm().getTitle(),
                "course", s.getForm().getCourse(),
                "submittedAt", s.getSubmittedAt().toString()
            )).collect(Collectors.toList())
        );
    }

    // GET /api/submissions/form/{formId}
    @GetMapping("/form/{formId}")
    public ResponseEntity<?> byForm(@PathVariable Long formId) {
        return ResponseEntity.ok(Map.of(
            "formId", formId,
            "count", submissionRepo.countByFormId(formId),
            "submissions", submissionRepo.findByFormId(formId).stream().map(s -> Map.of(
                "id", s.getId(),
                "student", s.getStudent().getName(),
                "submittedAt", s.getSubmittedAt().toString()
            )).collect(Collectors.toList())
        ));
    }

    @Data
    public static class SubmitRequest {
        private Long formId;
        private Long studentId;
        private Map<Long, Object> answers;
    }
}
