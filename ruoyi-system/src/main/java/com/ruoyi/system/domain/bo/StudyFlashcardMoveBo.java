package com.ruoyi.system.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 抽记卡移动请求对象
 * Flashcard move request object
 *
 * @author codex
 */
public class StudyFlashcardMoveBo
{
    /** 卡片ID */
    private Long itemId;

    /** 目标象限 */
    private Integer quadrantType;

    @NotNull(message = "卡片ID不能为空")
    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    @NotNull(message = "目标象限不能为空")
    @Min(value = 1, message = "目标象限最小为1")
    @Max(value = 4, message = "目标象限最大为4")
    public Integer getQuadrantType()
    {
        return quadrantType;
    }

    public void setQuadrantType(Integer quadrantType)
    {
        this.quadrantType = quadrantType;
    }
}
