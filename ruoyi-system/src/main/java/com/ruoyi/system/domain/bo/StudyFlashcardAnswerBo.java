package com.ruoyi.system.domain.bo;

import jakarta.validation.constraints.NotNull;

/**
 * 抽记卡答案保存请求对象
 * Flashcard answer save request object
 *
 * @author codex
 */
public class StudyFlashcardAnswerBo
{
    /** 卡片ID */
    private Long itemId;

    /** 最近输入答案 */
    private String lastUserAnswer;

    @NotNull(message = "卡片ID不能为空")
    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public String getLastUserAnswer()
    {
        return lastUserAnswer;
    }

    public void setLastUserAnswer(String lastUserAnswer)
    {
        this.lastUserAnswer = lastUserAnswer;
    }
}
