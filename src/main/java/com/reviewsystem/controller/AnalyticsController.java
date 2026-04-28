package com.reviewsystem.controller;

import com.reviewsystem.model.*;
import com.reviewsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired private FeedbackFormRepository formRepo;
    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private AnswerRepository answerRepo;

    // GET /api/analytics/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        long total = formRepo.count();
        long active = formRepo.findByStatus(FeedbackForm.Status.ACTIVE).size();
        long subs = submissionRepo.count();
        return ResponseEntity.ok(Map.of(
            "totalForms", total,
            "activeForms", active,
            "totalSubmissions", subs
        ));
    }

    // GET /api/analytics/{formId}
    @GetMapping("/{formId}")
    public ResponseEntity<?> formAnalytics(@PathVariable Long formId) {
        FeedbackForm form = formRepo.findById(formId)
            .orElseThrow(() -> new RuntimeException("Form not found"));

        int totalResponses = submissionRepo.countByFormId(formId);
        List<Question> questions = questionRepo.findByFormIdOrderByOrderIndex(formId);
        List<Map<String, Object>> questionAnalytics = new ArrayList<>();

        for (Question q : questions) {
            Map<String, Object> qa = new LinkedHashMap<>();
            qa.put("questionId", q.getId());
            qa.put("questionText", q.getText());
            qa.put("type", q.getType().name());
            List<Answer> answers = answerRepo.findByQuestionId(q.getId());

            if (q.getType() == Question.QuestionType.RATING) {
                int[] dist = new int[5];
                double sum = 0; int count = 0;
                for (Answer a : answers) {
                    try {
                        int v = Integer.parseInt(a.getAnswerValue().trim());
                        if (v >= 1 && v <= 5) { dist[v-1]++; sum += v; count++; }
                    } catch (Exception ignored) {}
                }
                qa.put("distribution", dist);
                qa.put("average", count > 0 ? Math.round(sum/count*10.0)/10.0 : 0);
                qa.put("count", count);

            } else if (q.getType() == Question.QuestionType.MCQ) {
                String[] opts = q.getOptions() != null ? q.getOptions().split(",") : new String[]{};
                Map<String, Integer> counts = new LinkedHashMap<>();
                for (String o : opts) counts.put(o.trim(), 0);
                for (Answer a : answers) {
                    try {
                        int idx = Integer.parseInt(a.getAnswerValue().trim());
                        if (idx >= 0 && idx < opts.length) {
                            String key = opts[idx].trim();
                            counts.put(key, counts.getOrDefault(key, 0) + 1);
                        }
                    } catch (Exception ignored) {}
                }
                qa.put("optionCounts", counts);
                qa.put("totalAnswers", answers.size());

            } else {
                List<String> texts = answers.stream()
                    .map(Answer::getAnswerValue)
                    .filter(v -> v != null && !v.isBlank())
                    .collect(Collectors.toList());
                qa.put("responses", texts);
                qa.put("count", texts.size());
            }
            questionAnalytics.add(qa);
        }

        double overallRating = questionAnalytics.stream()
            .filter(qa -> "RATING".equals(qa.get("type")))
            .mapToDouble(qa -> (double) qa.getOrDefault("average", 0.0))
            .average().orElse(0.0);

        return ResponseEntity.ok(Map.of(
            "formId", formId,
            "formTitle", form.getTitle(),
            "course", form.getCourse(),
            "totalResponses", totalResponses,
            "totalStudents", form.getTotalStudents(),
            "responseRate", form.getTotalStudents() > 0
                ? Math.round(totalResponses * 100.0 / form.getTotalStudents()) : 0,
            "averageRating", Math.round(overallRating * 10.0) / 10.0,
            "questionAnalytics", questionAnalytics
        ));
    }
}
