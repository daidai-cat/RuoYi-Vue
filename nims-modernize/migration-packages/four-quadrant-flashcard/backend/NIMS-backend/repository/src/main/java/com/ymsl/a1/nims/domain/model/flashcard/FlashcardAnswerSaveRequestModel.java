package com.ymsl.a1.nims.domain.model.flashcard;

import lombok.Data;

@Data
public class FlashcardAnswerSaveRequestModel {

    private Long itemId;

    private String lastUserAnswer;
}
