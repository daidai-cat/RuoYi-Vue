package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.StudyFlashcardBook;
import com.ruoyi.system.mapper.StudyFlashcardBookMapper;
import com.ruoyi.system.service.IStudyFlashcardBookService;
import com.ruoyi.system.service.IStudyFlashcardItemService;

/**
 * 抽记卡卡组服务实现
 * Flashcard book service implementation
 *
 * @author codex
 */
@Service
public class StudyFlashcardBookServiceImpl implements IStudyFlashcardBookService
{
    @Autowired
    private StudyFlashcardBookMapper studyFlashcardBookMapper;

    @Autowired
    private IStudyFlashcardItemService studyFlashcardItemService;

    @Override
    public List<StudyFlashcardBook> selectStudyFlashcardBookList(StudyFlashcardBook book)
    {
        book.setUserId(SecurityUtils.getUserId());
        return studyFlashcardBookMapper.selectStudyFlashcardBookList(book);
    }

    @Override
    public StudyFlashcardBook selectStudyFlashcardBookById(Long bookId)
    {
        return getOwnedBook(bookId);
    }

    @Override
    public int insertStudyFlashcardBook(StudyFlashcardBook book)
    {
        validateBookName(book.getBookName());
        fillCreateFields(book);
        if (StringUtils.isEmpty(book.getStatus()))
        {
            book.setStatus("0");
        }
        return studyFlashcardBookMapper.insertStudyFlashcardBook(book);
    }

    @Override
    public int updateStudyFlashcardBook(StudyFlashcardBook book)
    {
        validateBookName(book.getBookName());
        StudyFlashcardBook ownedBook = getOwnedBook(book.getBookId());
        book.setUserId(ownedBook.getUserId());
        book.setUpdateBy(SecurityUtils.getUsername());
        return studyFlashcardBookMapper.updateStudyFlashcardBook(book);
    }

    @Override
    public int deleteStudyFlashcardBookByIds(Long[] bookIds)
    {
        if (bookIds == null || bookIds.length == 0)
        {
            return 0;
        }
        // 校验每一个卡组都属于当前用户
        // Validate every book belongs to current user
        for (Long bookId : bookIds)
        {
            getOwnedBook(bookId);
        }
        studyFlashcardItemService.deleteStudyFlashcardItemByBookIds(bookIds, SecurityUtils.getUserId());
        return studyFlashcardBookMapper.deleteStudyFlashcardBookByIds(bookIds, SecurityUtils.getUserId());
    }

    /**
     * 校验并返回当前用户拥有的卡组
     * Validate and return the current user's owned flashcard book
     *
     * @param bookId 卡组ID
     * @return 卡组对象
     */
    private StudyFlashcardBook getOwnedBook(Long bookId)
    {
        StudyFlashcardBook query = new StudyFlashcardBook();
        query.setBookId(bookId);
        query.setUserId(SecurityUtils.getUserId());
        StudyFlashcardBook book = studyFlashcardBookMapper.selectStudyFlashcardBookByIdAndUserId(query);
        if (book == null)
        {
            throw new ServiceException("卡组不存在或无权访问");
        }
        return book;
    }

    /**
     * 填充创建人和所属用户
     * Fill creator and owner fields
     *
     * @param book 卡组对象
     */
    private void fillCreateFields(StudyFlashcardBook book)
    {
        book.setUserId(SecurityUtils.getUserId());
        book.setCreateBy(SecurityUtils.getUsername());
    }

    /**
     * 校验卡组名称
     * Validate flashcard book name
     *
     * @param bookName 卡组名称
     */
    private void validateBookName(String bookName)
    {
        if (StringUtils.isEmpty(bookName))
        {
            throw new ServiceException("卡组名称不能为空");
        }
    }
}
