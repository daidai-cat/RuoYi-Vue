package com.ymsl.a1.nims.domain.model.flashcard;

import lombok.Data;

@Data
public class FlashcardItemUpdateRequestModel {

    private Long itemId;

    private String frontContent;

    private String backContent;

    private Integer quadrantType;
}
