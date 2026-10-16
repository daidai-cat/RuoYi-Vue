ALTER TABLE public.flashcard_item
    ADD COLUMN IF NOT EXISTS last_user_answer TEXT;
