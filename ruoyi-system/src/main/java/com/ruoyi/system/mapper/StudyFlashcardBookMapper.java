package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.StudyFlashcardBook;

/**
 * 抽记卡卡组数据层
 * Flashcard book mapper
 *
 * @author codex
 */
public interface StudyFlashcardBookMapper
{
    /**
     * 查询卡组列表
     * Query flashcard book list
     *
     * @param book 查询条件
     * @return 卡组集合
     */
    public List<StudyFlashcardBook> selectStudyFlashcardBookList(StudyFlashcardBook book);

    /**
     * 根据ID查询卡组
     * Query flashcard book by ID
     *
     * @param bookId 卡组ID
     * @return 卡组信息
     */
    public StudyFlashcardBook selectStudyFlashcardBookById(Long bookId);

    /**
     * 根据ID和用户查询卡组
     * Query flashcard book by ID and user
     *
     * @param book 查询条件
     * @return 卡组信息
     */
    public StudyFlashcardBook selectStudyFlashcardBookByIdAndUserId(StudyFlashcardBook book);

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
    public int deleteStudyFlashcardBookByIds(@Param("bookIds") Long[] bookIds, @Param("userId") Long userId);
}
