package com.ymsl.a1.nims.web.controller.flashcard;

import com.ymsl.a1.nims.domain.entity.FlashcardBook;
import com.ymsl.a1.nims.domain.entity.FlashcardItem;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardAnswerSaveRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardBatchSaveRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardBookSaveModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardImportResultModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardItemDeleteRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardItemDetailRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardItemListRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardMoveRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardItemUpdateRequestModel;
import com.ymsl.a1.nims.domain.model.flashcard.FlashcardReviewScoreRequestModel;
import com.ymsl.a1.nims.service.flashcard.FlashcardService;
import com.ymsl.plugins.log.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 四象限抽记卡接口
 * Four-quadrant flashcard API
 */
@RestController
@RequestMapping("/flashcard")
public class FlashcardController {

    @Autowired
    private FlashcardService flashcardService;

    @PostMapping({"/book/list", "/book/list.json"})
    public List<FlashcardBook> listBooks() {
        return flashcardService.listBooks();
    }

    @PostMapping({"/book/save", "/book/save.json"})
    @LogOperation(value = "Save Flashcard Book", recordResult = true)
    public FlashcardBook saveBook(@RequestBody FlashcardBookSaveModel model) {
        return flashcardService.saveBook(model);
    }

    @PostMapping({"/item/list", "/item/list.json"})
    public List<FlashcardItem> listItems(@RequestBody FlashcardItemListRequestModel model) {
        return flashcardService.listItems(model.getBookId());
    }

    @PostMapping({"/item/batchSave", "/item/batchSave.json"})
    @LogOperation(value = "Batch Save Flashcard Items", recordResult = true)
    public List<FlashcardItem> batchSave(@RequestBody FlashcardBatchSaveRequestModel model) {
        return flashcardService.batchSaveItems(model.getBookId(), model.getQuadrantType(), model.getItems());
    }

    @PostMapping({"/item/importCsv", "/item/importCsv.json"})
    @LogOperation(value = "Import Flashcard CSV", recordResult = true)
    public FlashcardImportResultModel importCsv(@RequestParam("bookId") Long bookId, @RequestParam("file") MultipartFile file) {
        return new FlashcardImportResultModel(flashcardService.importCsv(bookId, file));
    }

    @PostMapping({"/item/move", "/item/move.json"})
    @LogOperation(value = "Move Flashcard Item", recordResult = true)
    public FlashcardItem moveItem(@RequestBody FlashcardMoveRequestModel model) {
        return flashcardService.moveItem(model.getItemId(), model.getQuadrantType());
    }

    @PostMapping({"/item/score", "/item/score.json"})
    @LogOperation(value = "Score Flashcard Item", recordResult = true)
    public FlashcardItem scoreItem(@RequestBody FlashcardReviewScoreRequestModel model) {
        return flashcardService.scoreItem(model.getItemId(), model.getScore());
    }

    @PostMapping({"/item/saveAnswer", "/item/saveAnswer.json"})
    @LogOperation(value = "Save Flashcard Answer", recordResult = true)
    public FlashcardItem saveAnswer(@RequestBody FlashcardAnswerSaveRequestModel model) {
        return flashcardService.saveAnswer(model.getItemId(), model.getLastUserAnswer());
    }

    @PostMapping({"/item/detail", "/item/detail.json"})
    public FlashcardItem detailItem(@RequestBody FlashcardItemDetailRequestModel model) {
        return flashcardService.detailItem(model.getItemId());
    }

    @PostMapping({"/item/update", "/item/update.json"})
    @LogOperation(value = "Update Flashcard Item", recordResult = true)
    public FlashcardItem updateItem(@RequestBody FlashcardItemUpdateRequestModel model) {
        return flashcardService.updateItem(model.getItemId(), model.getFrontContent(), model.getBackContent(), model.getQuadrantType());
    }

    @PostMapping({"/item/delete", "/item/delete.json"})
    @LogOperation(value = "Delete Flashcard Item", recordResult = true)
    public void deleteItem(@RequestBody FlashcardItemDeleteRequestModel model) {
        flashcardService.deleteItem(model.getItemId());
    }
}
