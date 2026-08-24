package com.adrovis.adrovis_backend.interview.entity;

import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionAnswerSource;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionCategory;
import com.adrovis.adrovis_backend.interview.enums.InterviewQuestionDifficulty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "interview_question",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_interview_question_number",
                        columnNames = {"interview_id", "question_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "interview_id", nullable = false)
    private UUID interviewId;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private InterviewQuestionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    private InterviewQuestionDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_source", nullable = false, length = 20)
    private InterviewQuestionAnswerSource answerSource;

    @Column(name = "question", nullable = false, columnDefinition = "text")
    private String question;

    @Column(name = "expected_answer", nullable = false, columnDefinition = "text")
    private String expectedAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_points", nullable = false, columnDefinition = "jsonb")
    private String keyPoints;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "follow_up_questions", nullable = false, columnDefinition = "jsonb")
    private String followUpQuestions;

    @Column(name = "resume_basis", columnDefinition = "text")
    private String resumeBasis;

    @Column(name = "interviewer_goal", nullable = false, columnDefinition = "text")
    private String interviewerGoal;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}