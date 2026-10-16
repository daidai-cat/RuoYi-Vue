# Study Flashcard Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a private-study flashcard feature under `学习 / 抽记卡` in the current `RuoYi-Vue` stack, including four-quadrant board, practice dialog, single-card CRUD, CSV import, and JSON batch import.

**Architecture:** The feature will be implemented as a standard RuoYi vertical slice: one SQL migration file, one menu seed section, backend `domain + mapper + service + controller`, and one Vue 2 + Element UI page with a dedicated API module. User isolation is enforced server-side with the current login user, while the frontend keeps the interaction in a single page with dialogs instead of separate routes.

**Tech Stack:** Vue 2, Element UI, Axios request wrapper, Spring Boot, RuoYi `BaseController` / `AjaxResult`, MyBatis XML, MySQL

**Project Constraints:** Per `AGENTS.md`, do not add JUnit or Vitest tests, and do not rely on compile/lint steps for completion. Verification in this plan uses SQL inspection, API smoke checks, and UI/manual workflow checks.

---

## File Structure

### New files

- `sql/ry_20260510_study_flashcard.sql`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/StudyFlashcardBook.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/StudyFlashcardItem.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardMoveBo.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardScoreBo.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardAnswerBo.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardBatchImportBo.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardJsonItemBo.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/StudyFlashcardImportResultVo.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/mapper/StudyFlashcardBookMapper.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/mapper/StudyFlashcardItemMapper.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/IStudyFlashcardBookService.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/IStudyFlashcardItemService.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/StudyFlashcardBookServiceImpl.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/StudyFlashcardItemServiceImpl.java`
- `ruoyi-system/src/main/resources/mapper/system/StudyFlashcardBookMapper.xml`
- `ruoyi-system/src/main/resources/mapper/system/StudyFlashcardItemMapper.xml`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/StudyFlashcardBookController.java`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/StudyFlashcardItemController.java`
- `ruoyi-ui/src/api/study/flashcard.js`
- `ruoyi-ui/src/views/study/flashcard/index.vue`

### Optional split files if `index.vue` grows too large

- `ruoyi-ui/src/views/study/flashcard/components/BookDialog.vue`
- `ruoyi-ui/src/views/study/flashcard/components/ItemDialog.vue`
- `ruoyi-ui/src/views/study/flashcard/components/PracticeDialog.vue`

### Existing files to reference, not modify unless needed

- `sql/ry_20260417.sql`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysMenuController.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/SysNotice.java`
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysNoticeServiceImpl.java`
- `ruoyi-system/src/main/resources/mapper/system/SysNoticeMapper.xml`
- `ruoyi-ui/src/api/system/notice.js`
- `ruoyi-ui/src/views/system/notice/index.vue`

## Chunk 1: Database And Menu Seed

### Task 1: Create the study flashcard SQL migration

**Files:**
- Create: `sql/ry_20260510_study_flashcard.sql`
- Reference: `sql/ry_20260417.sql`

- [ ] **Step 1: Add the table DDL for `study_flashcard_book`**

```sql
CREATE TABLE `study_flashcard_book` (
  `book_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Book ID',
  `user_id` bigint(20) NOT NULL COMMENT 'Owner user ID',
  `book_name` varchar(100) NOT NULL COMMENT 'Book name',
  `book_description` varchar(500) DEFAULT '' COMMENT 'Book description',
  `status` char(1) DEFAULT '0' COMMENT 'Status (0 normal 1 disabled)',
  `remark` varchar(500) DEFAULT NULL COMMENT 'Remark',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Create by',
  `create_time` datetime DEFAULT sysdate() COMMENT 'Create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'Update by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uk_study_flashcard_book_user_name` (`user_id`, `book_name`),
  KEY `idx_study_flashcard_book_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study flashcard book';
```

- [ ] **Step 2: Add the table DDL for `study_flashcard_item`**

```sql
CREATE TABLE `study_flashcard_item` (
  `item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Item ID',
  `book_id` bigint(20) NOT NULL COMMENT 'Book ID',
  `user_id` bigint(20) NOT NULL COMMENT 'Owner user ID',
  `front_content` text NOT NULL COMMENT 'Front content',
  `back_content` text NOT NULL COMMENT 'Back content',
  `quadrant_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT 'Quadrant 1-4',
  `review_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Review count',
  `difficulty_score` tinyint(4) DEFAULT NULL COMMENT 'Difficulty score',
  `last_user_answer` text COMMENT 'Last typed answer',
  `status` char(1) DEFAULT '0' COMMENT 'Status (0 normal 1 disabled)',
  `remark` varchar(500) DEFAULT NULL COMMENT 'Remark',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Create by',
  `create_time` datetime DEFAULT sysdate() COMMENT 'Create time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'Update by',
  `update_time` datetime DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (`item_id`),
  CONSTRAINT `ck_study_flashcard_item_quadrant` CHECK (`quadrant_type` between 1 and 4),
  KEY `idx_study_flashcard_item_user_book` (`user_id`, `book_id`),
  KEY `idx_study_flashcard_item_user_book_quadrant` (`user_id`, `book_id`, `quadrant_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study flashcard item';
```

- [ ] **Step 3: Add menu seed rows for `学习 / 抽记卡` and its permission buttons**

```sql
INSERT INTO `sys_menu` VALUES
('2000', '学习', '0', '5', 'study', '', '', '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '', NULL, '学习目录'),
('2001', '抽记卡', '2000', '1', 'flashcard', 'study/flashcard/index', '', '', 1, 0, 'C', '0', '0', 'study:flashcard:list', 'form', 'admin', sysdate(), '', NULL, '抽记卡菜单'),
('2002', '抽记卡查询', '2001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:query', '#', 'admin', sysdate(), '', NULL, ''),
('2003', '抽记卡新增', '2001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:add', '#', 'admin', sysdate(), '', NULL, ''),
('2004', '抽记卡修改', '2001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:edit', '#', 'admin', sysdate(), '', NULL, ''),
('2005', '抽记卡删除', '2001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:remove', '#', 'admin', sysdate(), '', NULL, ''),
('2006', '抽记卡导入', '2001', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:import', '#', 'admin', sysdate(), '', NULL, ''),
('2007', '抽记卡练习', '2001', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'study:flashcard:practice', '#', 'admin', sysdate(), '', NULL, '');
```

- [ ] **Step 4: Add rollback notes in SQL comments**
Expected: the file explains which rows belong to feature tables and which belong to `sys_menu`, so rollback is easy.

- [ ] **Step 5: Verify the migration file structure manually**
Run: `Get-Content .\sql\ry_20260510_study_flashcard.sql`
Expected: one self-contained SQL file with DDL first and menu inserts last.

- [ ] **Step 6: Commit**

```bash
git add sql/ry_20260510_study_flashcard.sql
git commit -m "feat(study): add flashcard schema and menu seed"
```

### Task 2: Decide the backend package placement and keep it aligned with existing RuoYi conventions

**Files:**
- Create later in: `ruoyi-system/src/main/java/com/ruoyi/system/**`
- Create later in: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/**`

- [ ] **Step 1: Keep entities under `com.ruoyi.system.domain`**
Expected: no extra Spring scanning changes are needed.

- [ ] **Step 2: Keep mappers under `com.ruoyi.system.mapper`**
Expected: XML namespace pattern matches current mapper layout.

- [ ] **Step 3: Keep services under `com.ruoyi.system.service` and `impl`**
Expected: implementation style mirrors `SysNotice`.

- [ ] **Step 4: Keep controllers under `com.ruoyi.web.controller.study`**
Expected: feature URLs are grouped but still live in `ruoyi-admin`.

- [ ] **Step 5: Commit**

```bash
git add docs/superpowers/specs/2026-05-10-study-flashcard-design.md docs/superpowers/plans/2026-05-10-study-flashcard.md
git commit -m "docs(study): add flashcard implementation plan"
```

## Chunk 2: Backend Domain And Mapper Layer

### Task 3: Create the book and item domain classes

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/StudyFlashcardBook.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/StudyFlashcardItem.java`
- Reference: `ruoyi-system/src/main/java/com/ruoyi/system/domain/SysNotice.java`

- [ ] **Step 1: Create `StudyFlashcardBook` extending `BaseEntity`**

```java
public class StudyFlashcardBook extends BaseEntity {
    private Long bookId;
    private Long userId;
    private String bookName;
    private String bookDescription;
    private String status;
}
```

- [ ] **Step 2: Create `StudyFlashcardItem` extending `BaseEntity`**

```java
public class StudyFlashcardItem extends BaseEntity {
    private Long itemId;
    private Long bookId;
    private Long userId;
    private String frontContent;
    private String backContent;
    private Integer quadrantType;
    private Integer reviewCount;
    private Integer difficultyScore;
    private String lastUserAnswer;
    private String status;
}
```

- [ ] **Step 3: Add validation annotations only where the API truly depends on them**
Expected: required fields are `bookName`, `frontContent`, `backContent`, and `quadrantType` when editing/moving.

- [ ] **Step 4: Add bilingual comments in non-obvious code blocks**
Expected: comments follow the repo rule, for example `// 获取卡组所属用户 / // Get flashcard book owner`.

- [ ] **Step 5: Verify entity field names match SQL column intent**
Run: `rg -n "bookId|itemId|bookName|frontContent|quadrantType|lastUserAnswer" .\ruoyi-system\src\main\java\com\ruoyi\system\domain`
Expected: entity fields align with the SQL migration and mapper XML names.

- [ ] **Step 6: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/domain/StudyFlashcardBook.java ruoyi-system/src/main/java/com/ruoyi/system/domain/StudyFlashcardItem.java
git commit -m "feat(study): add flashcard domain models"
```

### Task 4: Create request BOs and import result VO

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardMoveBo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardScoreBo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardAnswerBo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardBatchImportBo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/StudyFlashcardJsonItemBo.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/StudyFlashcardImportResultVo.java`

- [ ] **Step 1: Create move/score/answer BOs instead of overloading entity classes**

```java
public class StudyFlashcardMoveBo {
    private Long itemId;
    private Integer quadrantType;
}
```

- [ ] **Step 2: Create the JSON batch import BO**

```java
public class StudyFlashcardBatchImportBo {
    private Long bookId;
    private Integer quadrantType;
    private List<StudyFlashcardJsonItemBo> items;
}
```

- [ ] **Step 3: Create the import result VO**

```java
public class StudyFlashcardImportResultVo {
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private List<String> failureMessages;
}
```

- [ ] **Step 4: Keep these classes small and transport-focused**
Expected: no persistence logic or user ownership fields inside BO/VO objects.

- [ ] **Step 5: Verify package names compile mentally against controller imports**
Expected: controller code will only need request-specific objects, not raw `Map`.

- [ ] **Step 6: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/domain/bo ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/StudyFlashcardImportResultVo.java
git commit -m "feat(study): add flashcard request and response models"
```

### Task 5: Create mapper interfaces and XML files

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/StudyFlashcardBookMapper.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/StudyFlashcardItemMapper.java`
- Create: `ruoyi-system/src/main/resources/mapper/system/StudyFlashcardBookMapper.xml`
- Create: `ruoyi-system/src/main/resources/mapper/system/StudyFlashcardItemMapper.xml`
- Reference: `ruoyi-system/src/main/resources/mapper/system/SysNoticeMapper.xml`

- [ ] **Step 1: Add the book mapper interface with list/get/insert/update/delete methods**

```java
List<StudyFlashcardBook> selectStudyFlashcardBookList(StudyFlashcardBook book);
StudyFlashcardBook selectStudyFlashcardBookById(Long bookId);
int insertStudyFlashcardBook(StudyFlashcardBook book);
int updateStudyFlashcardBook(StudyFlashcardBook book);
int deleteStudyFlashcardBookByIds(Long[] bookIds);
```

- [ ] **Step 2: Add the item mapper interface with CRUD plus action methods**

```java
int moveStudyFlashcardItem(Long itemId, Integer quadrantType, String updateBy);
int updateStudyFlashcardItemScore(Long itemId, Integer difficultyScore, String updateBy);
int updateStudyFlashcardItemAnswer(Long itemId, String lastUserAnswer, String updateBy);
```

- [ ] **Step 3: Build XML result maps and reusable select SQL blocks**
Expected: XML uses explicit column aliases like current system mappers, not `select *`.

- [ ] **Step 4: Add user-scope filters in XML queries**
Expected: all list/detail queries include `user_id = #{userId}` either directly from entity or via dedicated mapper params.

- [ ] **Step 5: Add a safe cascade delete flow**
Expected: either a dedicated item delete by `bookIds` mapper method or service-first delete order before deleting books.

- [ ] **Step 6: Verify mapper namespaces and method names align**
Run: `rg -n "StudyFlashcard(Book|Item)Mapper" .\ruoyi-system\src\main\java .\ruoyi-system\src\main\resources\mapper\system`
Expected: interface names, XML namespaces, and method IDs all match exactly.

- [ ] **Step 7: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/mapper ruoyi-system/src/main/resources/mapper/system/StudyFlashcardBookMapper.xml ruoyi-system/src/main/resources/mapper/system/StudyFlashcardItemMapper.xml
git commit -m "feat(study): add flashcard mapper layer"
```

## Chunk 3: Backend Service And Controller Layer

### Task 6: Implement book and item services with owner isolation

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/IStudyFlashcardBookService.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/IStudyFlashcardItemService.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/StudyFlashcardBookServiceImpl.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/StudyFlashcardItemServiceImpl.java`
- Reference: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/SysNoticeServiceImpl.java`

- [ ] **Step 1: Define the service interfaces around user-scoped behavior**
Expected: method names describe feature actions, not generic `saveOrUpdate`.

- [ ] **Step 2: Implement a helper to inject current owner context**

```java
private void fillOwner(StudyFlashcardBook book, Long userId, String username) {
    book.setUserId(userId);
    book.setCreateBy(username);
}
```

- [ ] **Step 3: Enforce ownership checks before every item/book mutation**
Expected: item lookups confirm both `item_id` and `user_id`; book lookups confirm both `book_id` and `user_id`.

- [ ] **Step 4: Implement score updates without auto-moving quadrants**
Expected: score action updates `difficulty_score`, increments `review_count`, and leaves `quadrant_type` unchanged.

- [ ] **Step 5: Implement delete-book logic as `delete items first, then books`**
Expected: no orphan card rows remain after deleting a book.

- [ ] **Step 6: Implement JSON batch import in the service layer**
Expected: import code normalizes `frontContent/front/question` and `backContent/back/answer`, filters invalid rows, and returns counts plus messages.

- [ ] **Step 7: Implement CSV parsing in the service layer or a private helper**
Expected: first version supports two columns plus optional recognizable headers.

- [ ] **Step 8: Verify service signatures stay thin enough for controller use**
Run: `rg -n "interface IStudyFlashcard|class StudyFlashcard.*ServiceImpl" .\ruoyi-system\src\main\java`
Expected: book and item services expose only the actions required by the spec.

- [ ] **Step 9: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service ruoyi-system/src/main/java/com/ruoyi/system/service/impl
git commit -m "feat(study): add flashcard service layer"
```

### Task 7: Implement the REST controllers

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/StudyFlashcardBookController.java`
- Create: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/StudyFlashcardItemController.java`
- Reference: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/system/SysNoticeController.java`

- [ ] **Step 1: Create the book controller under `/study/flashcard/book`**
Expected: endpoints are `list`, `getInfo`, `add`, `edit`, `remove`.

- [ ] **Step 2: Create the item controller under `/study/flashcard/item`**
Expected: endpoints are `list`, `getInfo`, `add`, `edit`, `remove`, `move`, `score`, `answer`, `import/json`, `import/csv`.

- [ ] **Step 3: Use `@PreAuthorize` with the agreed permission codes**

```java
@PreAuthorize("@ss.hasPermi('study:flashcard:list')")
@GetMapping("/list")
public AjaxResult list(Long bookId) { ... }
```

- [ ] **Step 4: Use `getUserId()` and `getUsername()` from `BaseController`**
Expected: the frontend never submits `userId`; the controller supplies user context to the service.

- [ ] **Step 5: Keep multipart CSV import in the item controller**

```java
@PostMapping("/import/csv")
public AjaxResult importCsv(Long bookId, Integer quadrantType, MultipartFile file) { ... }
```

- [ ] **Step 6: Add bilingual comments around non-obvious owner-check or import logic**
Expected: comments satisfy the repo rule without explaining trivial setters/getters.

- [ ] **Step 7: Verify URL contracts against the spec**
Run: `rg -n "@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping" .\ruoyi-admin\src\main\java\com\ruoyi\web\controller\study`
Expected: every required endpoint exists once and only once.

- [ ] **Step 8: Commit**

```bash
git add ruoyi-admin/src/main/java/com/ruoyi/web/controller/study
git commit -m "feat(study): add flashcard controllers"
```

## Chunk 4: Frontend API And Page

### Task 8: Implement the frontend API module

**Files:**
- Create: `ruoyi-ui/src/api/study/flashcard.js`
- Reference: `ruoyi-ui/src/api/system/notice.js`
- Reference: `ruoyi-ui/src/utils/request.js`

- [ ] **Step 1: Add book API wrappers**

```javascript
export function listBook() {
  return request({ url: '/study/flashcard/book/list', method: 'get' })
}
```

- [ ] **Step 2: Add item CRUD and action wrappers**

```javascript
export function moveItem(data) {
  return request({ url: '/study/flashcard/item/move', method: 'put', data })
}
```

- [ ] **Step 3: Add multipart CSV import wrapper using `FormData`**
Expected: wrapper does not force JSON content type and lets Axios send multipart boundaries correctly.

- [ ] **Step 4: Add JSON batch import wrapper**
Expected: request body is `{ bookId, quadrantType, items }`.

- [ ] **Step 5: Verify API names map one-to-one to controller endpoints**
Run: `Get-Content .\ruoyi-ui\src\api\study\flashcard.js`
Expected: no leftover Vue 3 or `viy-ui` request patterns remain.

- [ ] **Step 6: Commit**

```bash
git add ruoyi-ui/src/api/study/flashcard.js
git commit -m "feat(study): add flashcard frontend api"
```

### Task 9: Implement the `学习 / 抽记卡` page in Vue 2 style

**Files:**
- Create: `ruoyi-ui/src/views/study/flashcard/index.vue`
- Optional create later: `ruoyi-ui/src/views/study/flashcard/components/*.vue`
- Reference: `ruoyi-ui/src/views/system/notice/index.vue`
- Reference feature: `nims-modernize/migration-packages/four-quadrant-flashcard/frontend/NIMS-ui/src/views/Flashcard/FourQuadrantFlashcard.vue`

- [ ] **Step 1: Scaffold the page with an `app-container` root and top toolbar**
Expected: layout looks native to current RuoYi pages.

- [ ] **Step 2: Build the book selection and management area**
Expected: page can select a book, open a book dialog, and refresh book data.

- [ ] **Step 3: Build the CSV import and JSON batch import panels**
Expected: both panels disable themselves when no book is selected.

- [ ] **Step 4: Build the four-column quadrant board**
Expected: columns are clearly labeled `Q1` to `Q4`, and card summaries show front content, review count, score, and last answer summary.

- [ ] **Step 5: Wire single-card CRUD dialogs**
Expected: card detail and edit stay inside the same page using `el-dialog`.

- [ ] **Step 6: Rebuild drag/move behavior in Vue 2**
Expected: dragging a card calls the move API and updates only the affected card state.

- [ ] **Step 7: Rebuild the practice dialog**

```javascript
data() {
  return {
    practiceOpen: false,
    practiceQueue: [],
    currentPracticeIndex: 0,
    answerDraft: '',
    practiceQuadrants: [1, 2, 3, 4],
    practiceOrderMode: 'sequence'
  }
}
```

- [ ] **Step 8: Preserve old feature rules without copying old UI structure**
Expected: keep flip/save-answer/retry/previous/next/score/manual-move, but use `Element UI` controls and current RuoYi message APIs.

- [ ] **Step 9: Use `$modal`, `$message`, and `$confirm` instead of `VueMessage`**
Expected: no `viy-ui` imports remain.

- [ ] **Step 10: Add bilingual comments only around tricky queue-sync and drag logic**
Expected: comments explain state synchronization, not obvious `if` blocks.

- [ ] **Step 11: Verify the page references only Vue 2-compatible patterns**
Run: `rg -n "script setup|defineOptions|ref\\(|computed\\(|onMounted|VueMessage|viy-ui" .\ruoyi-ui\src\views\study\flashcard`
Expected: no Vue 3 or `viy-ui` patterns remain.

- [ ] **Step 12: Commit**

```bash
git add ruoyi-ui/src/views/study/flashcard
git commit -m "feat(study): add flashcard page"
```

## Chunk 5: Integration And Smoke Verification

### Task 10: Connect menu data, owner behavior, and imports into one end-to-end flow

**Files:**
- Modify if needed: `sql/ry_20260510_study_flashcard.sql`
- Verify together: backend and frontend files from previous tasks

- [ ] **Step 1: Apply the SQL migration in the target database**
Expected: both feature tables exist and menu rows are inserted successfully.

- [ ] **Step 2: Assign the new menu/permissions to the intended role**
Expected: the current login user can see `学习 / 抽记卡`.

- [ ] **Step 3: Start the application in the normal local way**
Expected: backend and frontend are reachable with the feature menu visible.

- [ ] **Step 4: Smoke-check book APIs with manual requests**

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/study/flashcard/book/list -Headers @{ Authorization = "Bearer <token>" }
```

Expected: response code `200`, payload scoped to current user only.

- [ ] **Step 5: Smoke-check item list and item actions**
Expected: list/add/edit/delete/move/score/answer endpoints all return success payloads with no cross-user leakage.

- [ ] **Step 6: Manually verify the UI workflow**
Expected:
1. create book
2. add single card
3. import JSON cards
4. import CSV cards
5. drag a card between quadrants
6. open practice dialog
7. save an answer
8. score a card
9. retry a wrong card
10. delete a card

- [ ] **Step 7: Manually verify user isolation**
Expected: user A cannot see or mutate user B data by changing IDs in requests.

- [ ] **Step 8: Run a final repository grep for forbidden leftovers**
Run: `rg -n "viy-ui|VueMessage|script setup|/flashcard/.*\\.json" .\ruoyi-ui .\ruoyi-admin .\ruoyi-system`
Expected: no old migration-stack leftovers remain in the new implementation.

- [ ] **Step 9: Commit**

```bash
git add sql/ry_20260510_study_flashcard.sql ruoyi-admin ruoyi-system ruoyi-ui
git commit -m "feat(study): add private flashcard learning module"
```

## Execution Notes

- The implementation should follow the approved design in `docs/superpowers/specs/2026-05-10-study-flashcard-design.md`.
- Keep the first version single-page and vertically integrated; do not split into extra routes or analytics features.
- Do not add JUnit or Vitest files unless the project instruction changes.
- Do not add compile/lint steps to claim completion; use fresh SQL/API/UI verification instead.
- If `index.vue` becomes too large to review comfortably, split the dialogs into `components/` before continuing feature work.

## Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-10-study-flashcard.md`. Ready to execute?
