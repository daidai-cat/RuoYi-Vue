package com.ruoyi.system.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.system.domain.StudyFlashcardItem;
import com.ruoyi.system.domain.bo.StudyFlashcardAnswerBo;
import com.ruoyi.system.domain.bo.StudyFlashcardBatchImportBo;
import com.ruoyi.system.domain.bo.StudyFlashcardMoveBo;
import com.ruoyi.system.domain.bo.StudyFlashcardScoreBo;
import com.ruoyi.system.domain.vo.StudyFlashcardImportResultVo;

/**
 * 抽记卡卡片服务层
 * Flashcard item service
 *
 * @author codex
 */
public interface IStudyFlashcardItemService
{
    /**
     * 查询当前用户卡片列表
     * Query current user flashcard item list
     *
     * @param item 查询条件
     * @return 卡片集合
     */
    public List<StudyFlashcardItem> selectStudyFlashcardItemList(StudyFlashcardItem item);

    /**
     * 查询当前用户卡片详情
     * Query current user flashcard item detail
     *
     * @param itemId 卡片ID
     * @return 卡片信息
     */
    public StudyFlashcardItem selectStudyFlashcardItemById(Long itemId);

    /**
     * 新增卡片
     * Insert flashcard item
     *
     * @param item 卡片信息
     * @return 结果
     */
    public int insertStudyFlashcardItem(StudyFlashcardItem item);

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
    public int deleteStudyFlashcardItemByIds(Long[] itemIds);

    /**
     * 移动卡片
     * Move flashcard item
     *
     * @param bo 请求对象
     * @return 更新后的卡片
     */
    public StudyFlashcardItem moveStudyFlashcardItem(StudyFlashcardMoveBo bo);

    /**
     * 更新评分
     * Update flashcard score
     *
     * @param bo 请求对象
     * @return 更新后的卡片
     */
    public StudyFlashcardItem scoreStudyFlashcardItem(StudyFlashcardScoreBo bo);

    /**
     * 保存答案
     * Save flashcard answer
     *
     * @param bo 请求对象
     * @return 更新后的卡片
     */
    public StudyFlashcardItem saveStudyFlashcardAnswer(StudyFlashcardAnswerBo bo);

    /**
     * JSON 批量导入
     * JSON batch import
     *
     * @param bo 请求对象
     * @return 导入结果
     */
    public StudyFlashcardImportResultVo importStudyFlashcardJson(StudyFlashcardBatchImportBo bo);

    /**
     * CSV 导入
     * CSV import
     *
     * @param bookId 卡组ID
     * @param quadrantType 象限
     * @param file 上传文件
     * @return 导入结果
     */
    public StudyFlashcardImportResultVo importStudyFlashcardCsv(Long bookId, Integer quadrantType, MultipartFile file);

    /**
     * 按卡组删除卡片
     * Delete flashcard items by book IDs
     *
     * @param bookIds 卡组ID数组
     * @return 结果
     */
    public int deleteStudyFlashcardItemByBookIds(Long[] bookIds, Long userId);
}
