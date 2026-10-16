package com.ruoyi.system.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 抽记卡评分请求对象
 * Flashcard score request object
 *
 * @author codex
 */
public class StudyFlashcardScoreBo
{
    /** 卡片ID */
    private Long itemId;

    /** 难度评分 */
    private Integer difficultyScore;

    @NotNull(message = "卡片ID不能为空")
    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    public Integer getDifficultyScore()
    {
        return difficultyScore;
    }

    public void setDifficultyScore(Integer difficultyScore)
    {
        this.difficultyScore = difficultyScore;
    }
}
