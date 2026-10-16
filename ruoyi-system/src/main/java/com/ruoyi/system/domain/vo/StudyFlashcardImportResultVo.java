package com.ruoyi.system.domain.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽记卡导入结果对象
 * Flashcard import result object
 *
 * @author codex
 */
public class StudyFlashcardImportResultVo
{
    /** 总数 */
    private Integer totalCount = 0;

    /** 成功数 */
    private Integer successCount = 0;

    /** 失败数 */
    private Integer failureCount = 0;

    /** 失败信息 */
    private List<String> failureMessages = new ArrayList<String>();

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(Integer successCount)
    {
        this.successCount = successCount;
    }

    public Integer getFailureCount()
    {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount)
    {
        this.failureCount = failureCount;
    }

    public List<String> getFailureMessages()
    {
        return failureMessages;
    }

    public void setFailureMessages(List<String> failureMessages)
    {
        this.failureMessages = failureMessages;
    }
}
