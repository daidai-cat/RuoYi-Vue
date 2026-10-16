package com.ymsl.a1.nims.domain.entity;

import com.ymsl.solid.jpa.uuid.annotation.SnowflakeGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
 * 四象限抽记卡本
 * Four-quadrant flashcard book
 */
@Entity
@Table(name = "flashcard_book")
@DynamicInsert
@DynamicUpdate
@Setter
@Getter
@ToString
@EqualsAndHashCode(exclude = {"id"})
public class FlashcardBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SnowflakeGenerator
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        // 初始化创建时间
        // Initialize create time
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
