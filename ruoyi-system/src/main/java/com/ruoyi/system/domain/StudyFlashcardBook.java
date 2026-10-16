package com.ruoyi.system.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.xss.Xss;

/**
 * 学习抽记卡卡组对象 study_flashcard_book
 * Study flashcard book object study_flashcard_book
 *
 * @author codex
 */
public class StudyFlashcardBook extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 卡组ID */
    private Long bookId;

    /** 所属用户ID */
    private Long userId;

    /** 卡组名称 */
    private String bookName;

    /** 卡组描述 */
    private String bookDescription;

    /** 状态 */
    private String status;

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

    @Xss(message = "卡组名称不能包含脚本字符")
    @NotBlank(message = "卡组名称不能为空")
    @Size(min = 0, max = 100, message = "卡组名称长度不能超过100个字符")
    public String getBookName()
    {
        return bookName;
    }

    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }

    @Size(min = 0, max = 500, message = "卡组描述长度不能超过500个字符")
    public String getBookDescription()
    {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription)
    {
        this.bookDescription = bookDescription;
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
            .append("bookId", getBookId())
            .append("userId", getUserId())
            .append("bookName", getBookName())
            .append("bookDescription", getBookDescription())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
