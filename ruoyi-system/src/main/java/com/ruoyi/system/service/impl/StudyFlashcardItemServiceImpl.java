package com.ruoyi.system.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.StudyFlashcardBook;
import com.ruoyi.system.domain.StudyFlashcardItem;
import com.ruoyi.system.domain.bo.StudyFlashcardAnswerBo;
import com.ruoyi.system.domain.bo.StudyFlashcardBatchImportBo;
import com.ruoyi.system.domain.bo.StudyFlashcardJsonItemBo;
import com.ruoyi.system.domain.bo.StudyFlashcardMoveBo;
import com.ruoyi.system.domain.bo.StudyFlashcardScoreBo;
import com.ruoyi.system.domain.vo.StudyFlashcardImportResultVo;
import com.ruoyi.system.mapper.StudyFlashcardBookMapper;
import com.ruoyi.system.mapper.StudyFlashcardItemMapper;
import com.ruoyi.system.service.IStudyFlashcardItemService;

/**
 * 抽记卡卡片服务实现
 * Flashcard item service implementation
 *
 * @author codex
 */
@Service
public class StudyFlashcardItemServiceImpl implements IStudyFlashcardItemService
{
    @Autowired
    private StudyFlashcardItemMapper studyFlashcardItemMapper;

    @Autowired
    private StudyFlashcardBookMapper studyFlashcardBookMapper;

    @Override
    public List<StudyFlashcardItem> selectStudyFlashcardItemList(StudyFlashcardItem item)
    {
        item.setUserId(SecurityUtils.getUserId());
        if (item.getBookId() != null)
        {
            ensureOwnedBook(item.getBookId());
        }
        return studyFlashcardItemMapper.selectStudyFlashcardItemList(item);
    }

    @Override
    public StudyFlashcardItem selectStudyFlashcardItemById(Long itemId)
    {
        return getOwnedItem(itemId);
    }

    @Override
    public int insertStudyFlashcardItem(StudyFlashcardItem item)
    {
        ensureOwnedBook(item.getBookId());
        validateItem(item);
        fillCreateFields(item);
        if (item.getReviewCount() == null)
        {
            item.setReviewCount(0);
        }
        if (StringUtils.isEmpty(item.getStatus()))
        {
            item.setStatus("0");
        }
        return studyFlashcardItemMapper.insertStudyFlashcardItem(item);
    }

    @Override
    public int updateStudyFlashcardItem(StudyFlashcardItem item)
    {
        StudyFlashcardItem ownedItem = getOwnedItem(item.getItemId());
        if (item.getBookId() == null)
        {
            item.setBookId(ownedItem.getBookId());
        }
        ensureOwnedBook(item.getBookId());
        validateItem(item);
        item.setUserId(ownedItem.getUserId());
        item.setReviewCount(ownedItem.getReviewCount());
        item.setDifficultyScore(ownedItem.getDifficultyScore());
        item.setLastUserAnswer(ownedItem.getLastUserAnswer());
        item.setStatus(StringUtils.isEmpty(item.getStatus()) ? ownedItem.getStatus() : item.getStatus());
        item.setUpdateBy(SecurityUtils.getUsername());
        return studyFlashcardItemMapper.updateStudyFlashcardItem(item);
    }

    @Override
    public int deleteStudyFlashcardItemByIds(Long[] itemIds)
    {
        if (itemIds == null || itemIds.length == 0)
        {
            return 0;
        }
        for (Long itemId : itemIds)
        {
            getOwnedItem(itemId);
        }
        return studyFlashcardItemMapper.deleteStudyFlashcardItemByIds(itemIds, SecurityUtils.getUserId());
    }

    @Override
    public StudyFlashcardItem moveStudyFlashcardItem(StudyFlashcardMoveBo bo)
    {
        StudyFlashcardItem item = getOwnedItem(bo.getItemId());
        studyFlashcardItemMapper.moveStudyFlashcardItem(item.getItemId(), item.getUserId(), bo.getQuadrantType(),
            SecurityUtils.getUsername());
        return getOwnedItem(item.getItemId());
    }

    @Override
    public StudyFlashcardItem scoreStudyFlashcardItem(StudyFlashcardScoreBo bo)
    {
        StudyFlashcardItem item = getOwnedItem(bo.getItemId());
        studyFlashcardItemMapper.updateStudyFlashcardItemScore(item.getItemId(), item.getUserId(), bo.getDifficultyScore(),
            SecurityUtils.getUsername());
        return getOwnedItem(item.getItemId());
    }

    @Override
    public StudyFlashcardItem saveStudyFlashcardAnswer(StudyFlashcardAnswerBo bo)
    {
        StudyFlashcardItem item = getOwnedItem(bo.getItemId());
        studyFlashcardItemMapper.updateStudyFlashcardItemAnswer(item.getItemId(), item.getUserId(),
            StringUtils.nvl(bo.getLastUserAnswer(), ""), SecurityUtils.getUsername());
        return getOwnedItem(item.getItemId());
    }

    @Override
    public StudyFlashcardImportResultVo importStudyFlashcardJson(StudyFlashcardBatchImportBo bo)
    {
        ensureOwnedBook(bo.getBookId());
        validateQuadrantType(bo.getQuadrantType());
        List<StudyFlashcardItem> items = new ArrayList<StudyFlashcardItem>();
        StudyFlashcardImportResultVo result = new StudyFlashcardImportResultVo();
        result.setTotalCount(bo.getItems().size());
        for (int i = 0; i < bo.getItems().size(); i++)
        {
            StudyFlashcardJsonItemBo row = bo.getItems().get(i);
            String frontContent = normalizeFront(row);
            String backContent = normalizeBack(row);
            if (StringUtils.isEmpty(frontContent) || StringUtils.isEmpty(backContent))
            {
                appendFailure(result, i, "正反面内容不能为空");
                continue;
            }
            items.add(buildImportItem(bo.getBookId(), bo.getQuadrantType(), frontContent, backContent));
        }
        saveImportedItems(items, result);
        return result;
    }

    @Override
    public StudyFlashcardImportResultVo importStudyFlashcardCsv(Long bookId, Integer quadrantType, MultipartFile file)
    {
        ensureOwnedBook(bookId);
        validateQuadrantType(quadrantType);
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("CSV 文件不能为空");
        }
        StudyFlashcardImportResultVo result = new StudyFlashcardImportResultVo();
        List<StudyFlashcardItem> items = new ArrayList<StudyFlashcardItem>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line;
            int rowIndex = 0;
            int dataIndex = 0;
            while ((line = reader.readLine()) != null)
            {
                rowIndex++;
                String normalizedLine = stripUtf8Bom(line);
                if (StringUtils.isEmpty(normalizedLine))
                {
                    continue;
                }
                List<String> cells = parseCsvLine(normalizedLine);
                if (isCsvHeader(cells))
                {
                    continue;
                }
                dataIndex++;
                result.setTotalCount(result.getTotalCount() + 1);
                if (cells.size() < 2)
                {
                    appendFailure(result, dataIndex - 1, "CSV 至少需要两列");
                    continue;
                }
                String frontContent = stripCsvCell(cells.get(0));
                String backContent = stripCsvCell(cells.get(1));
                if (StringUtils.isEmpty(frontContent) || StringUtils.isEmpty(backContent))
                {
                    appendFailure(result, dataIndex - 1, "正反面内容不能为空");
                    continue;
                }
                items.add(buildImportItem(bookId, quadrantType, frontContent, backContent));
            }
        }
        catch (IOException e)
        {
            throw new ServiceException("读取 CSV 文件失败");
        }
        saveImportedItems(items, result);
        return result;
    }

    @Override
    public int deleteStudyFlashcardItemByBookIds(Long[] bookIds, Long userId)
    {
        return studyFlashcardItemMapper.deleteStudyFlashcardItemByBookIds(bookIds, userId);
    }

    /**
     * 获取当前用户拥有的卡片
     * Get the current user's owned flashcard item
     *
     * @param itemId 卡片ID
     * @return 卡片对象
     */
    private StudyFlashcardItem getOwnedItem(Long itemId)
    {
        StudyFlashcardItem query = new StudyFlashcardItem();
        query.setItemId(itemId);
        query.setUserId(SecurityUtils.getUserId());
        StudyFlashcardItem item = studyFlashcardItemMapper.selectStudyFlashcardItemByIdAndUserId(query);
        if (item == null)
        {
            throw new ServiceException("卡片不存在或无权访问");
        }
        return item;
    }

    /**
     * 校验卡组归属
     * Validate flashcard book ownership
     *
     * @param bookId 卡组ID
     * @return 卡组对象
     */
    private StudyFlashcardBook ensureOwnedBook(Long bookId)
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
     * 校验卡片基础字段
     * Validate flashcard item base fields
     *
     * @param item 卡片对象
     */
    private void validateItem(StudyFlashcardItem item)
    {
        if (item.getBookId() == null)
        {
            throw new ServiceException("所属卡组不能为空");
        }
        if (StringUtils.isEmpty(item.getFrontContent()))
        {
            throw new ServiceException("卡片正面内容不能为空");
        }
        if (StringUtils.isEmpty(item.getBackContent()))
        {
            throw new ServiceException("卡片背面内容不能为空");
        }
        validateQuadrantType(item.getQuadrantType());
    }

    /**
     * 填充创建字段和所属用户
     * Fill create fields and owner user
     *
     * @param item 卡片对象
     */
    private void fillCreateFields(StudyFlashcardItem item)
    {
        item.setUserId(SecurityUtils.getUserId());
        item.setCreateBy(SecurityUtils.getUsername());
    }

    /**
     * 构造导入卡片对象
     * Build imported flashcard item object
     *
     * @param bookId 卡组ID
     * @param quadrantType 象限
     * @param frontContent 正面内容
     * @param backContent 背面内容
     * @return 卡片对象
     */
    private StudyFlashcardItem buildImportItem(Long bookId, Integer quadrantType, String frontContent, String backContent)
    {
        StudyFlashcardItem item = new StudyFlashcardItem();
        item.setBookId(bookId);
        item.setUserId(SecurityUtils.getUserId());
        item.setFrontContent(frontContent);
        item.setBackContent(backContent);
        item.setQuadrantType(quadrantType);
        item.setReviewCount(0);
        item.setStatus("0");
        item.setCreateBy(SecurityUtils.getUsername());
        return item;
    }

    /**
     * 校验象限范围
     * Validate quadrant type range
     *
     * @param quadrantType 象限
     */
    private void validateQuadrantType(Integer quadrantType)
    {
        if (quadrantType == null || quadrantType < 1 || quadrantType > 4)
        {
            throw new ServiceException("象限类型必须在1到4之间");
        }
    }

    /**
     * 保存导入结果
     * Persist imported items and update result counters
     *
     * @param items 有效卡片列表
     * @param result 导入结果
     */
    private void saveImportedItems(List<StudyFlashcardItem> items, StudyFlashcardImportResultVo result)
    {
        if (!items.isEmpty())
        {
            studyFlashcardItemMapper.batchInsertStudyFlashcardItem(items);
        }
        result.setSuccessCount(items.size());
        result.setFailureCount(result.getFailureMessages().size());
    }

    /**
     * 追加失败信息
     * Append import failure message
     *
     * @param result 结果对象
     * @param index 行号
     * @param message 失败信息
     */
    private void appendFailure(StudyFlashcardImportResultVo result, int index, String message)
    {
        result.getFailureMessages().add("第" + (index + 1) + "条：" + message);
    }

    /**
     * 规范化正面内容
     * Normalize flashcard front content
     *
     * @param row JSON 行对象
     * @return 正面内容
     */
    private String normalizeFront(StudyFlashcardJsonItemBo row)
    {
        return stripCsvCell(StringUtils.defaultIfBlank(row.getFrontContent(),
            StringUtils.defaultIfBlank(row.getFront(), row.getQuestion())));
    }

    /**
     * 规范化背面内容
     * Normalize flashcard back content
     *
     * @param row JSON 行对象
     * @return 背面内容
     */
    private String normalizeBack(StudyFlashcardJsonItemBo row)
    {
        return stripCsvCell(StringUtils.defaultIfBlank(row.getBackContent(),
            StringUtils.defaultIfBlank(row.getBack(), row.getAnswer())));
    }

    /**
     * 去掉 CSV 包裹引号和空白
     * Trim CSV wrappers and surrounding whitespace
     *
     * @param value 原始值
     * @return 清理后的值
     */
    private String stripCsvCell(String value)
    {
        String normalized = StringUtils.trim(value);
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2)
        {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return StringUtils.trim(normalized);
    }

    /**
     * 去除 UTF-8 BOM 头，避免首列识别错误
     * Remove UTF-8 BOM header to avoid first-column parsing issues
     *
     * @param line 原始行内容
     * @return 处理后的行内容
     */
    private String stripUtf8Bom(String line)
    {
        if (line != null && !line.isEmpty() && line.charAt(0) == '\uFEFF')
        {
            return line.substring(1);
        }
        return line;
    }

    /**
     * 解析支持引号包裹的 CSV 行
     * Parse one CSV line with quoted-value support
     *
     * @param line CSV 原始行
     * @return 列数据
     */
    private List<String> parseCsvLine(String line)
    {
        List<String> cells = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++)
        {
            char ch = line.charAt(i);
            if (ch == '"')
            {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"')
                {
                    current.append('"');
                    i++;
                }
                else
                {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes)
            {
                cells.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        cells.add(current.toString());
        return cells;
    }

    /**
     * 判断当前行是否为表头
     * Detect whether the current CSV row is a header line
     *
     * @param cells 当前行列数据
     * @return 是否表头
     */
    private boolean isCsvHeader(List<String> cells)
    {
        if (cells == null || cells.size() < 2)
        {
            return false;
        }
        String frontHeader = stripCsvCell(cells.get(0)).toLowerCase();
        String backHeader = stripCsvCell(cells.get(1)).toLowerCase();
        return ("front".equals(frontHeader) || "frontcontent".equals(frontHeader) || "question".equals(frontHeader))
            && ("back".equals(backHeader) || "backcontent".equals(backHeader) || "answer".equals(backHeader));
    }
}
