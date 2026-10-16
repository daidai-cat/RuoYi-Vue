package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.StudyFlashcardItem;

/**
 * 抽记卡卡片数据层
 * Flashcard item mapper
 *
 * @author codex
 */
public interface StudyFlashcardItemMapper
{
    /**
     * 查询卡片列表
     * Query flashcard item list
     *
     * @param item 查询条件
     * @return 卡片集合
     */
    public List<StudyFlashcardItem> selectStudyFlashcardItemList(StudyFlashcardItem item);

    /**
     * 根据ID查询卡片
     * Query flashcard item by ID
     *
     * @param itemId 卡片ID
     * @return 卡片信息
     */
    public StudyFlashcardItem selectStudyFlashcardItemById(Long itemId);

    /**
     * 根据ID和用户查询卡片
     * Query flashcard item by ID and user
     *
     * @param item 查询条件
     * @return 卡片信息
     */
    public StudyFlashcardItem selectStudyFlashcardItemByIdAndUserId(StudyFlashcardItem item);

    /**
     * 新增卡片
     * Insert flashcard item
     *
     * @param item 卡片信息
     * @return 结果
     */
    public int insertStudyFlashcardItem(StudyFlashcardItem item);

    /**
     * 批量新增卡片
     * Batch insert flashcard items
     *
     * @param items 卡片集合
     * @return 结果
     */
    public int batchInsertStudyFlashcardItem(List<StudyFlashcardItem> items);

    /**
     * 修改卡片
     * Update flashcard item
     *
     * @param item 卡片信息
     * @return 结果
     */
    public int updateStudyFlashcardItem(StudyFlashcardItem item);

    /**
     * 删除卡片
     * Delete flashcard items
     *
     * @param itemIds 卡片ID数组
     * @return 结果
     */
    public int deleteStudyFlashcardItemByIds(@Param("itemIds") Long[] itemIds, @Param("userId") Long userId);

    /**
     * 按卡组删除卡片
     * Delete flashcard items by book IDs
     *
     * @param bookIds 卡组ID数组
     * @return 结果
     */
    public int deleteStudyFlashcardItemByBookIds(@Param("bookIds") Long[] bookIds, @Param("userId") Long userId);

    /**
     * 移动卡片到目标象限
     * Move flashcard item to target quadrant
     *
     * @param itemId 卡片ID
     * @param quadrantType 目标象限
     * @param updateBy 更新者
     * @return 结果
     */
    public int moveStudyFlashcardItem(@Param("itemId") Long itemId, @Param("userId") Long userId,
        @Param("quadrantType") Integer quadrantType, @Param("updateBy") String updateBy);

    /**
     * 更新卡片评分和复习次数
     * Update flashcard item score and review count
     *
     * @param itemId 卡片ID
     * @param difficultyScore 评分
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateStudyFlashcardItemScore(@Param("itemId") Long itemId, @Param("userId") Long userId,
        @Param("difficultyScore") Integer difficultyScore, @Param("updateBy") String updateBy);

    /**
     * 保存卡片最近答案
     * Save flashcard item latest answer
     *
     * @param itemId 卡片ID
     * @param lastUserAnswer 最近答案
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateStudyFlashcardItemAnswer(@Param("itemId") Long itemId, @Param("userId") Long userId,
        @Param("lastUserAnswer") String lastUserAnswer, @Param("updateBy") String updateBy);
}
