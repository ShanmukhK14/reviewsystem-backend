package com.reviewsystem.controller;

import com.reviewsystem.model.*;
import com.reviewsystem.repository.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forms")
@CrossOrigin(origins = "*")
public class FormController {

    @Autowired private FeedbackFormRepository formRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private AnswerRepository answerRepo;

    // GET /api/forms
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(formRepo.findAll().stream()
            .map(this::toMap).collect(Collectors.toList()));
    }

    // GET /api/forms/active
    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        return ResponseEntity.ok(
            formRepo.findByStatus(FeedbackForm.Status.ACTIVE).stream()
                .map(this::toMap).collect(Collectors.toList()));
    }

    // GET /api/forms/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return formRepo.findById(id)
            .map(f -> ResponseEntity.ok(toMap(f)))
            .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/forms/admin/{adminId}
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getByAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(formRepo.findByAdminId(adminId).stream()
            .map(this::toMap).collect(Collectors.toList()));
    }

    // POST /api/forms?adminId=1
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateFormRequest req,
                                     @RequestParam Long adminId) {
        User admin = userRepo.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        FeedbackForm form = formRepo.save(FeedbackForm.builder()
            .title(req.getTitle())
            .course(req.getCourse())
            .section(req.getSection())
            .openDate(req.getOpenDate())
            .closeDate(req.getCloseDate())
            .status(parseStatus(req.getStatus()))
            .totalStudents(req.getTotalStudents() > 0 ? req.getTotalStudents() : 60)
            .createdBy(admin.getName())
            .admin(admin)
            .build());

        if (req.getQuestions() != null) {
            int idx = 0;
            for (QuestionRequest qr : req.getQuestions()) {
                questionRepo.save(Question.builder()
                    .form(form).text(qr.getText())
                    .type(Question.QuestionType.valueOf(qr.getType().toUpperCase()))
                    .options(qr.getOptions() != null ? String.join(",", qr.getOptions()) : null)
                    .orderIndex(idx++).build());
            }
        }

        return ResponseEntity.ok(toMap(formRepo.findById(form.getId()).get()));
    }

    // PUT /api/forms/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @RequestBody Map<String, String> updates) {
        return formRepo.findById(id).map(form -> {
            if (updates.containsKey("status")) form.setStatus(parseStatus(updates.get("status")));
            if (updates.containsKey("title")) form.setTitle(updates.get("title"));
            if (updates.containsKey("course")) form.setCourse(updates.get("course"));
            formRepo.save(form);
            return ResponseEntity.ok(toMap(form));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/forms/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!formRepo.existsById(id)) return ResponseEntity.notFound().build();
        formRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Form deleted successfully"));
    }

    // ── Helper: form → map ─────────────────────────────────────
    private Map<String, Object> toMap(FeedbackForm form) {
        int responses = submissionRepo.countByFormId(form.getId());

        // Calculate average rating
        List<Question> ratingQs = questionRepo.findByFormIdAndType(form.getId(), Question.QuestionType.RATING);
        double avgRating = 0;
        if (!ratingQs.isEmpty() && responses > 0) {
            avgRating = ratingQs.stream().mapToDouble(q -> {
                List<Answer> ans = answerRepo.findByQuestionId(q.getId());
                return ans.stream().mapToInt(a -> {
                    try { return Integer.parseInt(a.getAnswerValue().trim()); } catch (Exception e) { return 0; }
                }).average().orElse(0);
            }).average().orElse(0);
            avgRating = Math.round(avgRating * 10.0) / 10.0;
        }

        // Questions
        List<Map<String, Object>> qs = questionRepo.findByFormIdOrderByOrderIndex(form.getId())
            .stream().map(q -> {
                Map<String, Object> qm = new LinkedHashMap<>();
                qm.put("id", q.getId());
                qm.put("text", q.getText());
                qm.put("type", q.getType().name());
                qm.put("orderIndex", q.getOrderIndex());
                if (q.getOptions() != null && !q.getOptions().isBlank())
                    qm.put("options", Arrays.asList(q.getOptions().split(",")));
                return qm;
            }).collect(Collectors.toList());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", form.getId());
        m.put("title", form.getTitle());
        m.put("course", form.getCourse());
        m.put("section", form.getSection() != null ? form.getSection() : "");
        m.put("openDate", form.getOpenDate() != null ? form.getOpenDate().toString() : "");
        m.put("closeDate", form.getCloseDate() != null ? form.getCloseDate().toString() : "");
        m.put("status", form.getStatus() != null ? form.getStatus().name() : "ACTIVE");
        m.put("totalStudents", form.getTotalStudents());
        m.put("responses", responses);
        m.put("rating", avgRating);
        m.put("createdBy", form.getCreatedBy() != null ? form.getCreatedBy() : "Admin");
        m.put("questions", qs);
        return m;
    }

    private FeedbackForm.Status parseStatus(String s) {
        if (s == null) return FeedbackForm.Status.ACTIVE;
        return switch (s.toUpperCase()) {
            case "CLOSED" -> FeedbackForm.Status.CLOSED;
            case "DRAFT"  -> FeedbackForm.Status.DRAFT;
            default       -> FeedbackForm.Status.ACTIVE;
        };
    }

    // ── Inner DTOs ─────────────────────────────────────────────
    @Data public static class CreateFormRequest {
        private String title, course, section, status;
        private LocalDate openDate, closeDate;
        private int totalStudents;
        private List<QuestionRequest> questions;
    }

    @Data public static class QuestionRequest {
        private String text, type;
        private List<String> options;
    }
}
