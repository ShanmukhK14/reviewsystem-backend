package com.reviewsystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private FeedbackForm form;

    @Column(nullable = false, length = 500)
    private String text;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(length = 1000)
    private String options; // comma separated for MCQ

    private int orderIndex;

    public enum QuestionType { RATING, MCQ, TEXT }
}
