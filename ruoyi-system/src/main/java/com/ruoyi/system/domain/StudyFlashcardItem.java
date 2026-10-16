package com.ruoyi.system.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学习抽记卡卡片对象 study_flashcard_item
 * Study flashcard item object study_flashcard_item
 *
 * @author codex
 */
public class StudyFlashcardItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 卡片ID */
    private Long itemId;

    /** 所属卡组ID */
    private Long bookId;

    /** 所属用户ID */
    private Long userId;

    /** 正面内容 */
    private String frontContent;

    /** 背面内容 */
    private String backContent;

    /** 象限类型 */
    private Integer quadrantType;

    /** 复习次数 */
    private Integer reviewCount;

    /** 难度评分 */
    private Integer difficultyScore;

    /** 最近一次输入答案 */
    private String lastUserAnswer;

    /** 状态 */
    private String status;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @NotBlank(message = "卡片正面内容不能为空")
    public String getFrontContent()
    {
        return frontContent;
    }

    public void setFrontContent(String frontContent)
    {
        this.frontContent = frontContent;
    }

    @NotBlank(message = "卡片背面内容不能为空")
    public String getBackContent()
    {
        return backContent;
    }

    public void setBackContent(String backContent)
    {
        this.backContent = backContent;
    }

    @Min(value = 1, message = "象限类型最小为1")
    @Max(value = 4, message = "象限类型最大为4")
    public Integer getQuadrantType()
    {
        return quadrantType;
    }

    public void setQuadrantType(Integer quadrantType)
    {
        this.quadrantType = quadrantType;
    }

    public Integer getReviewCount()
    {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount)
    {
        this.reviewCount = reviewCount;
    }

    public Integer getDifficultyScore()
    {
        return difficultyScore;
    }

    public void setDifficultyScore(Integer difficultyScore)
    {
        this.difficultyScore = difficultyScore;
    }

    public String getLastUserAnswer()
    {
        return lastUserAnswer;
    }

    public void setLastUserAnswer(String lastUserAnswer)
    {
        this.lastUserAnswer = lastUserAnswer;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("bookId", getBookId())
            .append("userId", getUserId())
            .append("frontContent", getFrontContent())
            .append("backContent", getBackContent())
            .append("quadrantType", getQuadrantType())
            .append("reviewCount", getReviewCount())
            .append("difficultyScore", getDifficultyScore())
            .append("lastUserAnswer", getLastUserAnswer())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
