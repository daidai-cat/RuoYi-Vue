package com.ruoyi.web.controller.study;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.StudyFlashcardItem;
import com.ruoyi.system.domain.bo.StudyFlashcardAnswerBo;
import com.ruoyi.system.domain.bo.StudyFlashcardBatchImportBo;
import com.ruoyi.system.domain.bo.StudyFlashcardMoveBo;
import com.ruoyi.system.domain.bo.StudyFlashcardScoreBo;
import com.ruoyi.system.service.IStudyFlashcardItemService;

/**
 * 抽记卡卡片控制器
 * Flashcard item controller
 *
 * @author codex
 */
@RestController
@RequestMapping("/study/flashcard/item")
public class StudyFlashcardItemController extends BaseController
{
    @Autowired
    private IStudyFlashcardItemService studyFlashcardItemService;

    /**
     * 查询当前用户卡片列表
     * Query current user flashcard item list
     *
     * @param item 查询条件
     * @return 卡片集合
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:list')")
    @GetMapping("/list")
    public AjaxResult list(StudyFlashcardItem item)
    {
        List<StudyFlashcardItem> list = studyFlashcardItemService.selectStudyFlashcardItemList(item);
        return success(list);
    }

    /**
     * 查询当前用户卡片详情
     * Query current user flashcard item detail
     *
     * @param itemId 卡片ID
     * @return 卡片详情
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:query')")
    @GetMapping("/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        return success(studyFlashcardItemService.selectStudyFlashcardItemById(itemId));
    }

    /**
     * 新增卡片
     * Insert flashcard item
     *
     * @param item 卡片信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:add')")
    @Log(title = "抽记卡卡片", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody StudyFlashcardItem item)
    {
        return toAjax(studyFlashcardItemService.insertStudyFlashcardItem(item));
    }

    /**
     * 修改卡片
     * Update flashcard item
     *
     * @param item 卡片信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:edit')")
    @Log(title = "抽记卡卡片", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody StudyFlashcardItem item)
    {
        return toAjax(studyFlashcardItemService.updateStudyFlashcardItem(item));
    }

    /**
     * 删除卡片
     * Delete flashcard items
     *
     * @param itemIds 卡片ID数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:remove')")
    @Log(title = "抽记卡卡片", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(studyFlashcardItemService.deleteStudyFlashcardItemByIds(itemIds));
    }

    /**
     * 移动卡片象限
     * Move flashcard item quadrant
     *
     * @param bo 请求对象
     * @return 更新后的卡片
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:edit')")
    @Log(title = "抽记卡卡片", businessType = BusinessType.UPDATE)
    @PutMapping("/move")
    public AjaxResult move(@Validated @RequestBody StudyFlashcardMoveBo bo)
    {
        return success(studyFlashcardItemService.moveStudyFlashcardItem(bo));
    }

    /**
     * 保存卡片评分
     * Save flashcard item score
     *
     * @param bo 请求对象
     * @return 更新后的卡片
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:practice')")
    @Log(title = "抽记卡练习评分", businessType = BusinessType.UPDATE)
    @PutMapping("/score")
    public AjaxResult score(@Validated @RequestBody StudyFlashcardScoreBo bo)
    {
        return success(studyFlashcardItemService.scoreStudyFlashcardItem(bo));
    }

    /**
     * 保存最近输入答案
     * Save latest typed answer
     *
     * @param bo 请求对象
     * @return 更新后的卡片
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:practice')")
    @Log(title = "抽记卡练习答案", businessType = BusinessType.UPDATE)
    @PutMapping("/answer")
    public AjaxResult answer(@Validated @RequestBody StudyFlashcardAnswerBo bo)
    {
        return success(studyFlashcardItemService.saveStudyFlashcardAnswer(bo));
    }

    /**
     * JSON 批量导入
     * JSON batch import
     *
     * @param bo 请求对象
     * @return 导入结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:import')")
    @Log(title = "抽记卡 JSON 导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import/json")
    public AjaxResult importJson(@Validated @RequestBody StudyFlashcardBatchImportBo bo)
    {
        return success(studyFlashcardItemService.importStudyFlashcardJson(bo));
    }

    /**
     * CSV 导入
     * CSV import
     *
     * @param bookId 卡组ID
     * @param quadrantType 象限
     * @param file 上传文件
     * @return 导入结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:import')")
    @Log(title = "抽记卡 CSV 导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import/csv")
    public AjaxResult importCsv(@RequestParam("bookId") Long bookId, @RequestParam("quadrantType") Integer quadrantType,
        @RequestParam("file") MultipartFile file)
    {
        return success(studyFlashcardItemService.importStudyFlashcardCsv(bookId, quadrantType, file));
    }
}
