package com.ymsl.a1.nims.domain.model.flashcard;

import lombok.Data;

import java.util.List;

@Data
public class FlashcardBatchSaveRequestModel {

    private Long bookId;

    private Integer quadrantType;

    private List<FlashcardItemSaveModel> items;
}
