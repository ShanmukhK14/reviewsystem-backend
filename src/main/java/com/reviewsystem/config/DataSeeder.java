package com.reviewsystem.config;

import com.reviewsystem.model.*;
import com.reviewsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepo;
    @Autowired private FeedbackFormRepository formRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private SubmissionRepository submissionRepo;
    @Autowired private AnswerRepository answerRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            System.out.println("✅ Database already has data — skipping seed");
            return;
        }

        System.out.println("🌱 Seeding ReviewSystem MySQL database...");

        // ── USERS ──────────────────────────────────────────────
        User admin = userRepo.save(User.builder()
            .name("Prof. Ravi Kumar")
            .email("professor@university.edu")
            .password(passwordEncoder.encode("password"))
            .role(User.Role.ADMIN).build());

        User student1 = userRepo.save(User.builder()
            .name("Jaswanth K.")
            .email("student@university.edu")
            .password(passwordEncoder.encode("password"))
            .role(User.Role.STUDENT).build());

        User student2 = userRepo.save(User.builder()
            .name("Priya S.")
            .email("priya@university.edu")
            .password(passwordEncoder.encode("password"))
            .role(User.Role.STUDENT).build());

        System.out.println("✅ Users seeded: " + admin.getName() + ", " + student1.getName() + ", " + student2.getName());

        // ── FORM 1: DBMS ───────────────────────────────────────
        FeedbackForm dbms = formRepo.save(FeedbackForm.builder()
            .title("DBMS Mid-Sem Feedback")
            .course("Database Management Systems")
            .section("Section A")
            .openDate(LocalDate.of(2026, 2, 24))
            .closeDate(LocalDate.of(2026, 4, 30))
            .status(FeedbackForm.Status.ACTIVE)
            .totalStudents(60).createdBy(admin.getName()).admin(admin).build());

        Question q1 = questionRepo.save(Question.builder().form(dbms)
            .text("How would you rate the overall teaching quality of this course?")
            .type(Question.QuestionType.RATING).orderIndex(0).build());
        Question q2 = questionRepo.save(Question.builder().form(dbms)
            .text("How would you describe the pace of the course?")
            .type(Question.QuestionType.MCQ).options("Too slow,Just right,Too fast").orderIndex(1).build());
        Question q3 = questionRepo.save(Question.builder().form(dbms)
            .text("What improvements would you suggest for this course?")
            .type(Question.QuestionType.TEXT).orderIndex(2).build());

        // ── FORM 2: OS ─────────────────────────────────────────
        FeedbackForm os = formRepo.save(FeedbackForm.builder()
            .title("OS Course Review")
            .course("Operating Systems")
            .section("Section B")
            .openDate(LocalDate.of(2026, 2, 20))
            .closeDate(LocalDate.of(2026, 4, 30))
            .status(FeedbackForm.Status.ACTIVE)
            .totalStudents(60).createdBy(admin.getName()).admin(admin).build());

        Question q4 = questionRepo.save(Question.builder().form(os)
            .text("Rate the course content and structure.")
            .type(Question.QuestionType.RATING).orderIndex(0).build());
        Question q5 = questionRepo.save(Question.builder().form(os)
            .text("How effective were the lab sessions?")
            .type(Question.QuestionType.MCQ).options("Very effective,Somewhat effective,Not effective").orderIndex(1).build());
        Question q6 = questionRepo.save(Question.builder().form(os)
            .text("Any suggestions for the instructor?")
            .type(Question.QuestionType.TEXT).orderIndex(2).build());

        // ── FORM 3: CN Lab (Closed with sample submissions) ────
        FeedbackForm cn = formRepo.save(FeedbackForm.builder()
            .title("CN Lab Evaluation")
            .course("Computer Networks Lab")
            .section("All Sections")
            .openDate(LocalDate.of(2026, 2, 1))
            .closeDate(LocalDate.of(2026, 2, 20))
            .status(FeedbackForm.Status.CLOSED)
            .totalStudents(60).createdBy(admin.getName()).admin(admin).build());

        Question q7 = questionRepo.save(Question.builder().form(cn)
            .text("Rate the lab equipment and infrastructure.")
            .type(Question.QuestionType.RATING).orderIndex(0).build());
        Question q8 = questionRepo.save(Question.builder().form(cn)
            .text("Were the lab experiments relevant to theory?")
            .type(Question.QuestionType.MCQ).options("Very relevant,Somewhat relevant,Not relevant").orderIndex(1).build());
        Question q9 = questionRepo.save(Question.builder().form(cn)
            .text("What improvements would you suggest for the lab?")
            .type(Question.QuestionType.TEXT).orderIndex(2).build());

        System.out.println("✅ Forms seeded: DBMS, OS, CN Lab");

        // ── SEED SAMPLE SUBMISSIONS for CN Lab ─────────────────
        String[][] sampleData = {
            {"4", "0", "Great lab sessions, very practical!"},
            {"5", "1", "Equipment could be better maintained."},
            {"3", "0", "More time needed for each experiment."},
            {"4", "2", "Lab manual was confusing at times."},
        };

        User[] students = {student1, student2, student1, student2};

        for (int i = 0; i < sampleData.length; i++) {
            User s = students[i];
            if (submissionRepo.existsByFormIdAndStudentId(cn.getId(), s.getId())) continue;

            Submission sub = submissionRepo.save(Submission.builder()
                .form(cn).student(s)
                .submittedAt(LocalDateTime.of(2026, 2, 18, 10 + i, 30)).build());

            answerRepo.save(Answer.builder().submission(sub).question(q7).answerValue(sampleData[i][0]).build());
            answerRepo.save(Answer.builder().submission(sub).question(q8).answerValue(sampleData[i][1]).build());
            answerRepo.save(Answer.builder().submission(sub).question(q9).answerValue(sampleData[i][2]).build());
        }

        System.out.println("✅ Sample submissions seeded");
        System.out.println("🚀 ReviewSystem MySQL database ready!");
        System.out.println("────────────────────────────────────");
        System.out.println("📌 API:    http://localhost:8080/api");
        System.out.println("📌 Admin:  professor@university.edu / password");
        System.out.println("📌 Student: student@university.edu / password");
        System.out.println("────────────────────────────────────");
    }
}
