package com.ruoyi.system.domain.bo;

import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 抽记卡 JSON 批量导入请求对象
 * Flashcard JSON batch import request object
 *
 * @author codex
 */
public class StudyFlashcardBatchImportBo
{
    /** 卡组ID */
    private Long bookId;

    /** 导入到的象限 */
    private Integer quadrantType;

    /** 导入项 */
    private List<StudyFlashcardJsonItemBo> items;

    @NotNull(message = "卡组ID不能为空")
    public Long getBookId()
    {
        return bookId;
    }

    public void setBookId(Long bookId)
    {
        this.bookId = bookId;
    }

    @NotNull(message = "象限不能为空")
    @Min(value = 1, message = "象限最小为1")
    @Max(value = 4, message = "象限最大为4")
    public Integer getQuadrantType()
    {
        return quadrantType;
    }

    public void setQuadrantType(Integer quadrantType)
    {
        this.quadrantType = quadrantType;
    }

    @NotEmpty(message = "导入数据不能为空")
    public List<StudyFlashcardJsonItemBo> getItems()
    {
        return items;
    }

    public void setItems(List<StudyFlashcardJsonItemBo> items)
    {
        this.items = items;
    }
}
