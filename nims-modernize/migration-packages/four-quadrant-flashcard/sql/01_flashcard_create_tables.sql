CREATE TABLE IF NOT EXISTS public.flashcard_book (
    id BIGINT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.flashcard_item (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    front_content TEXT NOT NULL,
    back_content TEXT NOT NULL,
    last_user_answer TEXT,
    quadrant_type INTEGER NOT NULL,
    difficulty_score INTEGER,
    last_review_time TIMESTAMP,
    review_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_flashcard_item_book
        FOREIGN KEY (book_id) REFERENCES public.flashcard_book (id),
    CONSTRAINT ck_flashcard_item_quadrant
        CHECK (quadrant_type BETWEEN 1 AND 4),
    CONSTRAINT ck_flashcard_item_score
        CHECK (difficulty_score IS NULL OR difficulty_score BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_flashcard_book_create_time
    ON public.flashcard_book (create_time DESC);

CREATE INDEX IF NOT EXISTS idx_flashcard_item_book_id
    ON public.flashcard_item (book_id);

CREATE INDEX IF NOT EXISTS idx_flashcard_item_book_quadrant
    ON public.flashcard_item (book_id, quadrant_type, id);
