package com.ruoyi.system.domain.bo;

/**
 * JSON 批量导入单项对象
 * JSON batch import row object
 *
 * @author codex
 */
public class StudyFlashcardJsonItemBo
{
    /** 正面内容 */
    private String frontContent;

    /** 兼容旧字段 */
    private String front;

    /** 兼容旧字段 */
    private String question;

    /** 背面内容 */
    private String backContent;

    /** 兼容旧字段 */
    private String back;

    /** 兼容旧字段 */
    private String answer;

    public String getFrontContent()
    {
        return frontContent;
    }

    public void setFrontContent(String frontContent)
    {
        this.frontContent = frontContent;
    }

    public String getFront()
    {
        return front;
    }

    public void setFront(String front)
    {
        this.front = front;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }

    public String getBackContent()
    {
        return backContent;
    }

    public void setBackContent(String backContent)
    {
        this.backContent = backContent;
    }

    public String getBack()
    {
        return back;
    }

    public void setBack(String back)
    {
        this.back = back;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }
}
