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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.StudyFlashcardBook;
import com.ruoyi.system.service.IStudyFlashcardBookService;

/**
 * 抽记卡卡组控制器
 * Flashcard book controller
 *
 * @author codex
 */
@RestController
@RequestMapping("/study/flashcard/book")
public class StudyFlashcardBookController extends BaseController
{
    @Autowired
    private IStudyFlashcardBookService studyFlashcardBookService;

    /**
     * 查询当前用户卡组列表
     * Query current user flashcard book list
     *
     * @param book 查询条件
     * @return 卡组集合
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:list')")
    @GetMapping("/list")
    public AjaxResult list(StudyFlashcardBook book)
    {
        List<StudyFlashcardBook> list = studyFlashcardBookService.selectStudyFlashcardBookList(book);
        return success(list);
    }

    /**
     * 查询当前用户卡组详情
     * Query current user flashcard book detail
     *
     * @param bookId 卡组ID
     * @return 卡组详情
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:query')")
    @GetMapping("/{bookId}")
    public AjaxResult getInfo(@PathVariable Long bookId)
    {
        return success(studyFlashcardBookService.selectStudyFlashcardBookById(bookId));
    }

    /**
     * 新增卡组
     * Insert flashcard book
     *
     * @param book 卡组信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:add')")
    @Log(title = "抽记卡卡组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody StudyFlashcardBook book)
    {
        return toAjax(studyFlashcardBookService.insertStudyFlashcardBook(book));
    }

    /**
     * 修改卡组
     * Update flashcard book
     *
     * @param book 卡组信息
     * @return 结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:edit')")
    @Log(title = "抽记卡卡组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody StudyFlashcardBook book)
    {
        return toAjax(studyFlashcardBookService.updateStudyFlashcardBook(book));
    }

    /**
     * 删除卡组
     * Delete flashcard books
     *
     * @param bookIds 卡组ID数组
     * @return 结果
     */
    @PreAuthorize("@ss.hasPermi('study:flashcard:remove')")
    @Log(title = "抽记卡卡组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bookIds}")
    public AjaxResult remove(@PathVariable Long[] bookIds)
    {
        return toAjax(studyFlashcardBookService.deleteStudyFlashcardBookByIds(bookIds));
    }
}
