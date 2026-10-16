# Four Quadrant Flashcard Migration Pack

This folder is a reusable migration pack for the current Four Quadrant Flashcard feature.
It is intended for:

- copying the feature into another Spring Boot + Vue 3 system
- handing the package to an AI agent for structured migration
- preserving the current frontend + backend + SQL implementation in one place

## Included Files

- `frontend/NIMS-ui/src/views/Flashcard/FourQuadrantFlashcard.vue`
  - Kanban board
  - CSV import
  - JSON batch injection
  - practice dialog
  - lightweight card CRUD
- `frontend/router/flashcard-route.example.js`
  - route registration example
- `backend/NIMS-backend/repository/.../entity`
  - `FlashcardBook`
  - `FlashcardItem`
- `backend/NIMS-backend/repository/.../model/flashcard`
  - request and response models
- `backend/NIMS-backend/repository/.../repository`
  - `FlashcardBookRepository`
  - `FlashcardItemRepository`
- `backend/NIMS-backend/service/.../flashcard/FlashcardService.java`
  - business logic
- `backend/NIMS-backend/web/.../flashcard/FlashcardController.java`
  - REST APIs
- `sql/01_flashcard_create_tables.sql`
  - full table creation
- `sql/02_flashcard_add_last_user_answer.sql`
  - incremental column patch

## Data Flow

Frontend page `FourQuadrantFlashcard.vue`
-> calls `/flashcard/**`
-> `FlashcardController`
-> `FlashcardService`
-> `FlashcardBookRepository / FlashcardItemRepository`
-> database tables `flashcard_book / flashcard_item`

## Frontend Dependencies

This page is not fully standalone. Check these project-level dependencies before migration:

- `@/utils/request`
- `VueMessage` from `viy-ui`
- Vue 3 with `script setup`
- component-scoped CSS

If the target project does not have these:

- replace `request(...)` with the target HTTP wrapper
- replace `showMessage(...)` with the target message component

## Frontend Route Setup

See `frontend/router/flashcard-route.example.js`.

Typical integration:

1. Copy the page to `src/views/Flashcard/`
2. Register route `/Flashcard/FourQuadrantFlashcard`
3. Point the target menu entry to that route

## Backend Dependencies

Current backend code depends on:

- Spring Boot
- Spring MVC
- Spring Data JPA
- Lombok
- `BusinessCodedException`
- `@LogOperation`
- `@SnowflakeGenerator`

If the target project does not have these exact classes:

- map `BusinessCodedException` to the target business exception type
- remove or replace `@LogOperation`
- replace `@SnowflakeGenerator` with the target ID generation strategy

## API List

### Book APIs

- `POST /flashcard/book/list.json`
- `POST /flashcard/book/save.json`

### Item APIs

- `POST /flashcard/item/list.json`
- `POST /flashcard/item/batchSave.json`
- `POST /flashcard/item/importCsv.json`
- `POST /flashcard/item/move.json`
- `POST /flashcard/item/score.json`
- `POST /flashcard/item/saveAnswer.json`
- `POST /flashcard/item/detail.json`
- `POST /flashcard/item/update.json`
- `POST /flashcard/item/delete.json`

## SQL Order

For a brand new project:

1. run `sql/01_flashcard_create_tables.sql`

For an existing project that already has `flashcard_item` but misses the answer field:

1. run `sql/02_flashcard_add_last_user_answer.sql`

## Migration Options

### Option A: Full migration

Use this when the target system is close to the current Spring Boot + Vue 3 structure.

1. copy `frontend`
2. copy `backend`
3. run SQL
4. register route
5. configure menu
6. replace project-specific dependencies

### Option B: Frontend first

Use this when you want the UI and interaction first.

1. copy `FourQuadrantFlashcard.vue`
2. replace APIs with mock or temporary APIs
3. migrate backend later

### Option C: Backend first

Use this when the target project already has its own frontend style.

1. copy `entity / model / repository / service / controller`
2. run SQL
3. rebuild the page in the target frontend stack

## Known Behavior

- current UI text is mostly English
- score is stored, but does not auto-change quadrant
- wrong-card retry is handled in the frontend practice queue
- practice dialog includes:
  - card flip
  - answer save
  - retry on wrong
  - previous / next
  - score buttons
  - manual quadrant move
  - lightweight view / edit / delete

## Recommended Migration Order

1. create tables
2. migrate backend APIs
3. migrate frontend page
4. connect route and menu

This order makes debugging simpler because issues usually fall into one of these buckets:

- route wiring
- API wiring
- database schema
- project-level dependency mismatch
