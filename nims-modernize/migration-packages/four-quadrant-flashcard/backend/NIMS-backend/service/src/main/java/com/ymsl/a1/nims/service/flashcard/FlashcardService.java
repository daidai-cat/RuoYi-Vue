package com.ymsl.a1.nims.service.flashcard;

import com.ymsl.a1.nims.domain.entity.FlashcardBook;
import com.ymsl.a1.nims.domain.entity.FlashcardItem;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardBookSaveModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardItemSaveModel;
import com.ymsl.a1.nims.repository.FlashcardBookRepository;
import com.ymsl.a1.nims.repository.FlashcardItemRepository;
import com.ymsl.solid.base.exception.BusinessCodedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 鍥涜薄闄愭娊璁板崱涓氬姟鏈嶅姟
 * Four-quadrant flashcard business service
 */
@Service
public class FlashcardService {

    private static final int DEFAULT_QUADRANT = 1;

    @Autowired
    private FlashcardBookRepository flashcardBookRepository;

    @Autowired
    private FlashcardItemRepository flashcardItemRepository;

    public List<FlashcardBook> listBooks() {
        return flashcardBookRepository.findAllByOrderByCreateTimeDesc();
    }

    @Transactional
    public FlashcardBook saveBook(FlashcardBookSaveModel model) {
        validateBook(model);
        FlashcardBook book = model.getId() == null
                ? new FlashcardBook()
                : flashcardBookRepository.findById(model.getId())
                .orElseThrow(() -> new BusinessCodedException("Flashcard book not found"));
        book.setName(model.getName());
        book.setDescription(model.getDescription());
        return flashcardBookRepository.save(book);
    }

    public List<FlashcardItem> listItems(Long bookId) {
        validateBookId(bookId);
        return flashcardItemRepository.findByBookIdOrderByQuadrantTypeAscIdAsc(bookId);
    }

    @Transactional
    public List<FlashcardItem> batchSaveItems(Long bookId, Integer quadrantType, List<FlashcardItemSaveModel> itemModels) {
        validateBookId(bookId);
        int normalizedQuadrant = normalizeQuadrant(quadrantType);
        List<FlashcardItem> items = new ArrayList<>();
        if (itemModels == null || itemModels.isEmpty()) {
            return items;
        }
        for (FlashcardItemSaveModel itemModel : itemModels) {
            if (itemModel == null || isBlank(itemModel.getFrontContent()) || isBlank(itemModel.getBackContent())) {
                continue;
            }
            items.add(buildItem(bookId, itemModel.getFrontContent(), itemModel.getBackContent(), normalizedQuadrant));
        }
        return flashcardItemRepository.saveAll(items);
    }

    @Transactional
    public int importCsv(Long bookId, MultipartFile file) {
        validateBookId(bookId);
        if (file == null || file.isEmpty()) {
            throw new BusinessCodedException("CSV file is required");
        }
        List<FlashcardItem> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> columns = parseCsvLine(line);
                if (columns.size() < 2 || isBlank(columns.get(0)) || isBlank(columns.get(1))) {
                    continue;
                }
                items.add(buildItem(bookId, columns.get(0), columns.get(1), DEFAULT_QUADRANT));
            }
        } catch (IOException e) {
            throw new BusinessCodedException("Failed to read CSV file: " + e.getMessage());
        }
        flashcardItemRepository.saveAll(items);
        return items.size();
    }

    @Transactional
    public FlashcardItem moveItem(Long itemId, Integer quadrantType) {
        FlashcardItem item = getItem(itemId);
        item.setQuadrantType(normalizeQuadrant(quadrantType));
        return flashcardItemRepository.save(item);
    }

    @Transactional
    public FlashcardItem scoreItem(Long itemId, Integer score) {
        FlashcardItem item = getItem(itemId);
        int normalizedScore = normalizeScore(score);
        item.setDifficultyScore(normalizedScore);
        item.setLastReviewTime(LocalDateTime.now());
        item.setReviewCount(item.getReviewCount() == null ? 1 : item.getReviewCount() + 1);
        return flashcardItemRepository.save(item);
    }

    @Transactional
    public FlashcardItem saveAnswer(Long itemId, String lastUserAnswer) {
        FlashcardItem item = getItem(itemId);
        item.setLastUserAnswer(lastUserAnswer);
        return flashcardItemRepository.save(item);
    }

    public FlashcardItem detailItem(Long itemId) {
        return getItem(itemId);
    }

    @Transactional
    public FlashcardItem updateItem(Long itemId, String frontContent, String backContent, Integer quadrantType) {
        FlashcardItem item = getItem(itemId);
        validateItemContent(frontContent, backContent);
        item.setFrontContent(frontContent);
        item.setBackContent(backContent);
        item.setQuadrantType(normalizeQuadrant(quadrantType));
        return flashcardItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        FlashcardItem item = getItem(itemId);
        flashcardItemRepository.delete(item);
    }

    private FlashcardItem buildItem(Long bookId, String frontContent, String backContent, Integer quadrantType) {
        FlashcardItem item = new FlashcardItem();
        item.setBookId(bookId);
        item.setFrontContent(frontContent);
        item.setBackContent(backContent);
        item.setQuadrantType(quadrantType);
        item.setReviewCount(0);
        return item;
    }

    private FlashcardItem getItem(Long itemId) {
        if (itemId == null) {
            throw new BusinessCodedException("Flashcard item id is required");
        }
        return flashcardItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessCodedException("Flashcard item not found"));
    }

    private int normalizeScore(Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new BusinessCodedException("Score must be between 1 and 5");
        }
        return score;
    }

    private int normalizeQuadrant(Integer quadrantType) {
        if (quadrantType == null || quadrantType < 1 || quadrantType > 4) {
            throw new BusinessCodedException("Quadrant type must be between 1 and 4");
        }
        return quadrantType;
    }

    private void validateBook(FlashcardBookSaveModel model) {
        if (model == null || isBlank(model.getName())) {
            throw new BusinessCodedException("Flashcard book name is required");
        }
    }

    private void validateItemContent(String frontContent, String backContent) {
        if (isBlank(frontContent)) {
            throw new BusinessCodedException("Flashcard front content is required");
        }
        if (isBlank(backContent)) {
            throw new BusinessCodedException("Flashcard back content is required");
        }
    }

    private void validateBookId(Long bookId) {
        if (bookId == null) {
            throw new BusinessCodedException("Flashcard book id is required");
        }
        if (!flashcardBookRepository.existsById(bookId)) {
            throw new BusinessCodedException("Flashcard book not found");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private List<String> parseCsvLine(String line) {
        // 瑙ｆ瀽鍗曡CSV锛屾敮鎸佸弻寮曞彿鍜岄€楀彿杞箟
        // Parse one CSV line with quote and comma escaping
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            if (currentChar == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == ',' && !inQuotes) {
                columns.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }
        columns.add(current.toString().trim());
        return columns;
    }
}

