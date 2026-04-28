package com.reviewsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "feedback_forms")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackForm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String course;

    private String section;
    private LocalDate openDate;
    private LocalDate closeDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int totalStudents;
    private String createdBy;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions;

    public enum Status { ACTIVE, CLOSED, DRAFT }
}
