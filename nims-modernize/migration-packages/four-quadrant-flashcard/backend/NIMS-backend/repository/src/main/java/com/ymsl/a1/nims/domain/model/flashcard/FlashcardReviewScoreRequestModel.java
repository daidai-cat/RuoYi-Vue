package com.ymsl.a1.nims.domain.model.flashcard;

import lombok.Data;

@Data
public class FlashcardReviewScoreRequestModel {

    private Long itemId;

    private Integer score;

    private Integer quadrantType;
}
