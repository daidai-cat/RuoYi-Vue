package com.ymsl.a1.nims.domain.entity;

import com.ymsl.solid.jpa.uuid.annotation.SnowflakeGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 四象限抽记卡明细
 * Four-quadrant flashcard item
 */
@Entity
@Table(name = "flashcard_item")
@DynamicInsert
@DynamicUpdate
@Setter
@Getter
@ToString
@EqualsAndHashCode(exclude = {"id"})
public class FlashcardItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SnowflakeGenerator
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Lob
    @Column(name = "front_content", nullable = false)
    private String frontContent;

    @Lob
    @Column(name = "back_content", nullable = false)
    private String backContent;

    @Lob
    @Column(name = "last_user_answer")
    private String lastUserAnswer;

    @Column(name = "quadrant_type", nullable = false)
    private Integer quadrantType;

    @Column(name = "difficulty_score")
    private Integer difficultyScore;

    @Column(name = "last_review_time")
    private LocalDateTime lastReviewTime;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;
}
