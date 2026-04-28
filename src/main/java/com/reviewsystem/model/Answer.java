package com.reviewsystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_value", length = 2000)
    private String answerValue;
}
