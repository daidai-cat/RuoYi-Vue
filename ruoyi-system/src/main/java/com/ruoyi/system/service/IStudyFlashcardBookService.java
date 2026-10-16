package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.StudyFlashcardBook;

/**
 * 抽记卡卡组服务层
 * Flashcard book service
 *
 * @author codex
 */
public interface IStudyFlashcardBookService
{
    /**
     * 查询当前用户卡组列表
     * Query current user flashcard book list
     *
     * @param book 查询条件
     * @return 卡组集合
     */
    public List<StudyFlashcardBook> selectStudyFlashcardBookList(StudyFlashcardBook book);

    /**
     * 查询当前用户卡组详情
     * Query current user flashcard book detail
     *
     * @param bookId 卡组ID
     * @return 卡组信息
     */
    public StudyFlashcardBook selectStudyFlashcardBookById(Long bookId);

    /**
     * 新增卡组
     * Insert flashcard book
     *
     * @param book 卡组信息
     * @return 结果
     */
    public int insertStudyFlashcardBook(StudyFlashcardBook book);

    /**
     * 修改卡组
     * Update flashcard book
     *
     * @param book 卡组信息
     * @return 结果
     */
    public int updateStudyFlashcardBook(StudyFlashcardBook book);

    /**
     * 删除卡组
     * Delete flashcard books
     *
     * @param bookIds 卡组ID数组
     * @return 结果
     */
    public int deleteStudyFlashcardBookByIds(Long[] bookIds);
}
