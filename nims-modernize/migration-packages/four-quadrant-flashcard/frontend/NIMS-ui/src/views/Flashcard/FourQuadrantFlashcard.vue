<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { VueMessage } from 'viy-ui';
import { request } from '@/utils/request';

defineOptions({
  name: 'FourQuadrantFlashcard',
});

const quadrants = [
  { type: 1, title: 'Q1 Important / Hard', subtitle: 'High value, not familiar', accent: '#ef4444' },
  { type: 2, title: 'Q2 Important / Familiar', subtitle: 'High value, keep fresh', accent: '#3b82f6' },
  { type: 3, title: 'Q3 Optional / Hard', subtitle: 'Lower priority, not familiar', accent: '#f97316' },
  { type: 4, title: 'Q4 Optional / Familiar', subtitle: 'Lower priority, archived', accent: '#22c55e' },
];

const books = ref([]);
const items = ref([]);
const selectedBookId = ref(null);
const bookName = ref('');
const bookDescription = ref('');
const jsonText = ref('');
const batchQuadrantType = ref(1);
const csvFile = ref(null);
const draggedItemId = ref(null);
const practiceVisible = ref(false);
const currentPracticeIndex = ref(0);
const flipped = ref(false);
const practiceQueue = ref([]);
const answerDraft = ref('');
const practiceOrderMode = ref('sequence');
const practiceQuadrants = ref([1, 2, 3, 4]);
const practiceFeedback = ref(null);
const itemDialogVisible = ref(false);
const itemDialogMode = ref('view');
const itemForm = ref({
  itemId: null,
  frontContent: '',
  backContent: '',
  quadrantType: 1,
  reviewCount: 0,
  difficultyScore: null,
  lastUserAnswer: '',
});
let practiceFeedbackTimer = null;

const showMessage = (type, message) => {
  // 缂傚倸鍊烽懗鍫曞窗瀹ュ洨鍗氱痪鎵祾Y婵犵數鍋為崹鐢告偋婵犲啫顕遍柛娑卞灡鐎氼剟鏌涢幇鍏哥凹闁?
  // Use unified VIY message invocation
  VueMessage({
    type,
    message,
  });
};

const groupedItems = computed(() => {
  return quadrants.reduce((result, quadrant) => {
    result[quadrant.type] = items.value.filter(item => item.quadrantType === quadrant.type);
    return result;
  }, {});
});

const currentPracticeItem = computed(() => practiceQueue.value[currentPracticeIndex.value] || null);

const currentPracticeQuadrant = computed(() => {
  return quadrants.find(quadrant => quadrant.type === currentPracticeItem.value?.quadrantType) || null;
});

const currentPracticeScore = computed(() => currentPracticeItem.value?.difficultyScore || null);

const isAllQuadrantsSelected = computed(() => practiceQuadrants.value.length === quadrants.length);

const normalizeApiPayload = (response) => {
  // CN: normalize axios response or intercepted business data
  // Normalize axios response or intercepted business data
  const payload = response?.data ?? response;
  if (payload?.success === true && Object.prototype.hasOwnProperty.call(payload, 'data')) {
    return payload.data;
  }
  if (payload?.success === true && Object.prototype.hasOwnProperty.call(payload, 'message')) {
    return payload.message;
  }
  return payload;
};

const apiPost = async (url, data) => {
  const response = await request(url, {
    method: 'post',
    data,
  });
  return normalizeApiPayload(response);
};

const asArray = (value) => {
  // CN: keep list state safe when API returns empty or wrapped data
  // Keep list state safe when API returns empty or wrapped data
  return Array.isArray(value) ? value : [];
};

const syncItemState = (updatedItem) => {
  if (!updatedItem?.id) {
    return;
  }
  // CN: sync updated card into kanban list and current practice queue
  // Sync updated card into kanban list and current practice queue
  items.value = items.value.map(item => item.id === updatedItem.id ? updatedItem : item);
  practiceQueue.value = practiceQueue.value.map(item => item.id === updatedItem.id ? updatedItem : item);
};

const removeItemState = (itemId) => {
  items.value = items.value.filter(item => item.id !== itemId);
  practiceQueue.value = practiceQueue.value.filter(item => item.id !== itemId);
};

const markCurrentPracticeResult = (result) => {
  if (!currentPracticeItem.value?.id) {
    return;
  }
  practiceQueue.value = practiceQueue.value.map((item, index) => {
    if (index !== currentPracticeIndex.value) {
      return item;
    }
    return {
      ...item,
      practiceResult: result,
    };
  });
};

const resetPracticeInput = () => {
  // CN: clear current typed answer when moving to another card
  // Clear current typed answer when moving to another card
  answerDraft.value = '';
  flipped.value = false;
  practiceFeedback.value = null;
};

const togglePracticeQuadrant = (quadrantType) => {
  if (practiceQuadrants.value.includes(quadrantType)) {
    practiceQuadrants.value = practiceQuadrants.value.filter(type => type !== quadrantType);
  } else {
    practiceQuadrants.value = [...practiceQuadrants.value, quadrantType].sort((a, b) => a - b);
  }
};

const toggleAllPracticeQuadrants = () => {
  practiceQuadrants.value = isAllQuadrantsSelected.value ? [] : quadrants.map(quadrant => quadrant.type);
};

const shuffleItems = (sourceItems) => {
  const result = [...sourceItems];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
};

const loadBooks = async () => {
  books.value = asArray(await apiPost('/flashcard/book/list.json', {}));
  if (!selectedBookId.value && books.value.length > 0) {
    selectedBookId.value = books.value[0].id;
    await loadItems();
  }
};

const loadItems = async () => {
  if (!selectedBookId.value) {
    items.value = [];
    return;
  }
  items.value = asArray(await apiPost('/flashcard/item/list.json', {
    bookId: selectedBookId.value,
  }));
};

const saveBook = async () => {
  if (!bookName.value) {
    showMessage('warning', 'Please enter book name');
    return;
  }
  const book = await apiPost('/flashcard/book/save.json', {
    name: bookName.value,
    description: bookDescription.value,
  });
  selectedBookId.value = book?.id;
  bookName.value = '';
  bookDescription.value = '';
  await loadBooks();
  await loadItems();
};

const handleBookChange = async () => {
  await loadItems();
};

const handleFileChange = (event) => {
  csvFile.value = event.target.files?.[0] || null;
};

const importCsv = async () => {
  if (!selectedBookId.value || !csvFile.value) {
    showMessage('warning', 'Please select book and CSV file');
    return;
  }
  const formData = new FormData();
  formData.append('bookId', selectedBookId.value);
  formData.append('file', csvFile.value);
  const response = await request('/flashcard/item/importCsv.json', {
    method: 'post',
    data: formData,
  });
  const result = normalizeApiPayload(response);
  showMessage('success', `Imported ${result?.importedCount || 0} cards`);
  await loadItems();
};

const parseJsonItems = () => {
  const parsed = JSON.parse(jsonText.value);
  if (!Array.isArray(parsed)) {
    throw new Error('JSON must be an array');
  }
  return parsed
    .map((row) => {
      // 闂備胶顭堢换鎺楀储瑜旈、娆撳箛椤撶偟绐炲┑顔斤耿绾顭囧鑸靛€垫繛鎴炵懐濞堟洘銇勯弬璺ㄧ劯鐎?      // Support multiple field names
      const frontContent = row.frontContent || row.front || row.question || row[0];
      const backContent = row.backContent || row.back || row.answer || row[1];
      return { frontContent, backContent };
    })
    .filter(row => row.frontContent && row.backContent);
};

const batchSaveJson = async () => {
  if (!selectedBookId.value) {
    showMessage('warning', 'Please select book');
    return;
  }
  try {
    const jsonItems = parseJsonItems();
    if (jsonItems.length === 0) {
      showMessage('warning', 'No valid card data');
      return;
    }
    await apiPost('/flashcard/item/batchSave.json', {
      bookId: selectedBookId.value,
      quadrantType: Number(batchQuadrantType.value),
      items: jsonItems,
    });
    jsonText.value = '';
    await loadItems();
  } catch (error) {
    showMessage('error', error.message || 'Invalid JSON data');
  }
};

const startDrag = (item) => {
  draggedItemId.value = item.id;
};

const dropToQuadrant = async (quadrantType) => {
  if (!draggedItemId.value) {
    return;
  }
  const itemId = draggedItemId.value;
  draggedItemId.value = null;
  const updatedItem = await apiPost('/flashcard/item/move.json', {
    itemId,
    quadrantType,
  });
  syncItemState(updatedItem);
  await loadItems();
};

const openPractice = () => {
  if (practiceQuadrants.value.length === 0) {
    showMessage('warning', 'Please select practice quadrant');
    return;
  }
  const sourceItems = items.value.filter(item => practiceQuadrants.value.includes(item.quadrantType));
  if (sourceItems.length === 0) {
    showMessage('warning', 'No flashcards to practice');
    return;
  }
  practiceQueue.value = practiceOrderMode.value === 'random' ? shuffleItems(sourceItems) : [...sourceItems];
  currentPracticeIndex.value = 0;
  resetPracticeInput();
  practiceVisible.value = true;
};

const closePractice = () => {
  practiceVisible.value = false;
  practiceQueue.value = [];
  currentPracticeIndex.value = 0;
  clearTimeout(practiceFeedbackTimer);
  practiceFeedbackTimer = null;
  resetPracticeInput();
};

const scoreCurrentItem = async (score) => {
  if (!currentPracticeItem.value) {
    return;
  }
  const updatedItem = await apiPost('/flashcard/item/score.json', {
    itemId: currentPracticeItem.value.id,
    score,
  });
  syncItemState(updatedItem);
  showMessage('success', `Score ${score} saved`);
};

const saveCurrentAnswer = async () => {
  if (!currentPracticeItem.value) {
    return;
  }
  // 濞ｅ洦绻傞悺銊╁嫉椤掑嫷鏆柡鍫墯椤愬吋娼忛幘鍐插汲濞戞捁妗ㄧ粭鍌氣枎閳ヨ櫕绀€缂?
  // Save current input as the latest answer for this card
  const updatedItem = await apiPost('/flashcard/item/saveAnswer.json', {
    itemId: currentPracticeItem.value.id,
    lastUserAnswer: answerDraft.value,
  });
  syncItemState(updatedItem);
  flipped.value = true;
  showMessage('success', 'Answer saved');
};

const goPracticeCard = (offset) => {
  if (practiceFeedback.value) {
    return;
  }
  const nextIndex = currentPracticeIndex.value + offset;
  if (nextIndex < 0 || nextIndex >= practiceQueue.value.length) {
    return;
  }
  currentPracticeIndex.value = nextIndex;
  resetPracticeInput();
};

const advancePracticeCard = () => {
  if (currentPracticeIndex.value + 1 >= practiceQueue.value.length) {
    showMessage('success', 'Practice round completed');
    closePractice();
    return;
  }
  currentPracticeIndex.value += 1;
  resetPracticeInput();
};

const playPracticeFeedback = (type, afterFeedback) => {
  if (practiceFeedback.value) {
    return;
  }
  practiceFeedback.value = type;
  clearTimeout(practiceFeedbackTimer);
  practiceFeedbackTimer = window.setTimeout(() => {
    practiceFeedback.value = null;
    afterFeedback();
  }, 650);
};

const completePracticeCard = () => {
  markCurrentPracticeResult('known');
  playPracticeFeedback('known', advancePracticeCard);
};

const retryPracticeCard = () => {
  if (!currentPracticeItem.value || practiceFeedback.value) {
    return;
  }
  // 闂佹寧鐟╅。鑺ユ交閽樺顫ｉ柛鎺斿濠€鐗堟姜椤曗偓濡诧妇浜?
  // Append wrong card to the end of this practice queue
  const retryItem = {
    ...currentPracticeItem.value,
    practiceResult: 'retry',
  };
  markCurrentPracticeResult('retry');
  practiceQueue.value.push(retryItem);
  playPracticeFeedback('retry', advancePracticeCard);
};

const moveCurrentPracticeItem = async (quadrantType) => {
  if (!currentPracticeItem.value) {
    return;
  }
  const updatedItem = await apiPost('/flashcard/item/move.json', {
    itemId: currentPracticeItem.value.id,
    quadrantType,
  });
  syncItemState(updatedItem);
  await loadItems();
  showMessage('success', 'Quadrant updated');
};

const openItemDialog = (item, mode = 'view') => {
  // CN: keep the lightweight CRUD modal focused on one card
  // Keep the lightweight CRUD modal focused on one card
  itemDialogMode.value = mode;
  itemForm.value = {
    itemId: item.id,
    frontContent: item.frontContent || '',
    backContent: item.backContent || '',
    quadrantType: item.quadrantType || 1,
    reviewCount: item.reviewCount || 0,
    difficultyScore: item.difficultyScore || null,
    lastUserAnswer: item.lastUserAnswer || '',
  };
  itemDialogVisible.value = true;
};

const closeItemDialog = () => {
  itemDialogVisible.value = false;
  itemDialogMode.value = 'view';
};

const saveItemDialog = async () => {
  if (!itemForm.value.itemId) {
    return;
  }
  const updatedItem = await apiPost('/flashcard/item/update.json', {
    itemId: itemForm.value.itemId,
    frontContent: itemForm.value.frontContent,
    backContent: itemForm.value.backContent,
    quadrantType: Number(itemForm.value.quadrantType),
  });
  syncItemState(updatedItem);
  closeItemDialog();
  showMessage('success', 'Card updated');
};

const deleteItem = async (item) => {
  if (!item?.id) {
    return;
  }
  if (!window.confirm('Delete this card?')) {
    return;
  }
  await apiPost('/flashcard/item/delete.json', {
    itemId: item.id,
  });
  removeItemState(item.id);
  if (itemDialogVisible.value && itemForm.value.itemId === item.id) {
    closeItemDialog();
  }
  showMessage('success', 'Card deleted');
};

const shouldIgnorePracticeHotkey = (target) => {
  const tagName = target?.tagName?.toLowerCase();
  return tagName === 'input' || tagName === 'textarea' || tagName === 'select' || target?.isContentEditable;
};

const handlePracticeHotkey = (event) => {
  if (!practiceVisible.value || practiceFeedback.value || shouldIgnorePracticeHotkey(event.target)) {
    return;
  }
  if (event.key === 'Escape') {
    event.preventDefault();
    closePractice();
  } else if (event.key === 'ArrowLeft') {
    event.preventDefault();
    goPracticeCard(-1);
  } else if (event.key === 'ArrowRight') {
    event.preventDefault();
    goPracticeCard(1);
  } else if (event.code === 'Space') {
    event.preventDefault();
    flipped.value = !flipped.value;
  }
};

onMounted(async () => {
  window.addEventListener('keydown', handlePracticeHotkey);
  await loadBooks();
});

onBeforeUnmount(() => {
  clearTimeout(practiceFeedbackTimer);
  window.removeEventListener('keydown', handlePracticeHotkey);
});
</script>

<template>
  <div class="flashcard-page">
    <section class="flashcard-toolbar">
      <div class="toolbar-block">
        <label>Book</label>
        <select v-model="selectedBookId" class="glass-input" @change="handleBookChange">
          <option v-for="book in books" :key="book.id" :value="book.id">
            {{ book.name }}
          </option>
        </select>
      </div>
      <div class="toolbar-block grow">
        <label>New Book</label>
        <div class="inline-controls">
          <input v-model="bookName" class="glass-input" placeholder="Book name">
          <input v-model="bookDescription" class="glass-input" placeholder="Description">
          <button class="primary-btn" @click="saveBook">
            Save
          </button>
        </div>
      </div>
      <div class="toolbar-block practice-config">
        <label>Practice Range</label>
        <div class="practice-options">
          <button
            class="option-chip"
            :class="{ active: isAllQuadrantsSelected }"
            @click="toggleAllPracticeQuadrants"
          >
            All
          </button>
          <button
            v-for="quadrant in quadrants"
            :key="quadrant.type"
            class="option-chip"
            :class="{ active: practiceQuadrants.includes(quadrant.type) }"
            :style="{ '--accent-color': quadrant.accent }"
            @click="togglePracticeQuadrant(quadrant.type)"
          >
            Q{{ quadrant.type }}
          </button>
          <select v-model="practiceOrderMode" class="glass-input order-select">
            <option value="sequence">
              Sequence
            </option>
            <option value="random">
              Random
            </option>
          </select>
        </div>
      </div>
      <button class="practice-btn" @click="openPractice">
        Practice
      </button>
    </section>

    <section class="inject-panel">
      <div class="inject-card">
        <div class="inject-header">
          <span>CSV Import</span>
          <small>First column question, second column answer</small>
        </div>
        <div class="inline-controls">
          <input class="glass-input file-input" type="file" accept=".csv,text/csv" @change="handleFileChange">
          <button class="secondary-btn" @click="importCsv">
            Import CSV
          </button>
        </div>
      </div>

      <div class="inject-card">
        <div class="inject-header">
          <span>JSON Injection</span>
          <small>Array: frontContent/backContent or question/answer</small>
        </div>
        <textarea v-model="jsonText" class="json-input" placeholder='[{"frontContent":"Question","backContent":"Answer"}]' />
        <div class="inline-controls">
          <select v-model="batchQuadrantType" class="glass-input compact">
            <option v-for="quadrant in quadrants" :key="quadrant.type" :value="quadrant.type">
              {{ quadrant.title }}
            </option>
          </select>
          <button class="secondary-btn" @click="batchSaveJson">
            Save JSON
          </button>
        </div>
      </div>
    </section>

    <section class="kanban-board">
      <article
        v-for="quadrant in quadrants"
        :key="quadrant.type"
        class="quadrant-column"
        :style="{ '--accent-color': quadrant.accent }"
        @dragover.prevent
        @drop="dropToQuadrant(quadrant.type)"
      >
        <header class="quadrant-header">
          <div>
            <h3>{{ quadrant.title }}</h3>
            <p>{{ quadrant.subtitle }}</p>
          </div>
          <strong>{{ groupedItems[quadrant.type]?.length || 0 }}</strong>
        </header>
        <div class="card-list">
          <div
            v-for="item in groupedItems[quadrant.type]"
            :key="item.id"
            class="flashcard-tile"
            draggable="true"
            @dragstart="startDrag(item)"
          >
            <p>{{ item.frontContent }}</p>
            <div class="tile-footer">
              <span>Review {{ item.reviewCount || 0 }} | Score {{ item.difficultyScore || '-' }}</span>
              <div class="tile-toolbar">
                <button class="tile-icon-btn" title="View" aria-label="View card" @click.stop="openItemDialog(item, 'view')">
                  i
                </button>
                <button class="tile-icon-btn" title="Edit" aria-label="Edit card" @click.stop="openItemDialog(item, 'edit')">
                  &#9998;
                </button>
                <button class="tile-icon-btn danger" title="Delete" aria-label="Delete card" @click.stop="deleteItem(item)">
                  &times;
                </button>
              </div>
            </div>
          </div>
        </div>
      </article>
    </section>

    <div v-if="practiceVisible" class="practice-overlay">
      <div class="practice-shell">
        <button class="close-btn" @click="closePractice">
          x
        </button>
        <div class="flip-scene">
          <div class="practice-progress">
            {{ currentPracticeIndex + 1 }} / {{ practiceQueue.length }}
          </div>
          <div
            v-if="currentPracticeItem?.practiceResult && !flipped"
            class="feedback-status"
            :class="currentPracticeItem.practiceResult"
          >
            {{ currentPracticeItem.practiceResult === 'known' ? 'Got it' : 'Not sure' }}
          </div>
          <div class="flip-card" :class="{ flipped }" @click="flipped = !flipped">
            <div class="flip-face front">
              {{ currentPracticeItem?.frontContent }}
            </div>
            <div class="flip-face back">
              <div class="answer-compare">
                <div class="answer-block user-answer">
                  <span>My Answer</span>
                  <p>{{ currentPracticeItem?.lastUserAnswer || '' }}</p>
                </div>
                <div class="answer-block standard-answer">
                  <span>Standard Answer</span>
                  <p>{{ currentPracticeItem?.backContent }}</p>
                </div>
              </div>
            </div>
          </div>
          <div
            v-if="practiceFeedback"
            class="feedback-card"
            :class="practiceFeedback"
          >
              <span>{{ currentPracticeItem.practiceResult === 'known' ? 'Got it' : 'Not sure' }}</span>          </div>
        </div>
        <div class="answer-input-row">
          <textarea
            v-model="answerDraft"
            class="practice-answer-input"
            placeholder="Type your answer"
          />
          <button class="secondary-btn answer-save-btn" title="Save answer" aria-label="Save answer" @click="saveCurrentAnswer">
            &#10003;
          </button>
        </div>
        <div v-if="flipped" class="score-panel">
          <div class="score-summary">
            <span>Score</span>
            <strong>{{ currentPracticeScore || '-' }}</strong>
          </div>
          <div class="score-options">
            <button
              v-for="score in 5"
              :key="score"
              class="score-btn"
              :class="{ active: currentPracticeScore === score }"
              :aria-pressed="currentPracticeScore === score"
              @click="scoreCurrentItem(score)"
            >
              {{ score }}
            </button>
          </div>
        </div>
        <div class="practice-control-bar">
          <div class="move-actions">
            <span
              class="current-quadrant-badge"
              :style="{ '--accent-color': currentPracticeQuadrant?.accent || '#334155' }"
            >
              Current Q{{ currentPracticeQuadrant?.type || '-' }}
            </span>
            <button
              v-for="quadrant in quadrants"
              :key="quadrant.type"
              class="move-btn"
              :class="{ active: currentPracticeItem?.quadrantType === quadrant.type }"
              :style="{ '--accent-color': quadrant.accent }"
              @click="moveCurrentPracticeItem(quadrant.type)"
            >
              Q{{ quadrant.type }}
            </button>
          </div>
          <div class="judge-actions">
            <button
              class="round-nav-btn"
              :disabled="currentPracticeIndex === 0 || Boolean(practiceFeedback)"
              title="Previous"
              aria-label="Previous card"
              @click="goPracticeCard(-1)"
            >
              <span aria-hidden="true">&larr;</span>
            </button>
            <button
              class="judge-pill wrong-btn"
              :disabled="Boolean(practiceFeedback)"
              title="Retry"
              aria-label="Retry this card"
              @click="retryPracticeCard"
            >
              <span>&#10005;</span>
            </button>
            <button
              class="judge-pill right-btn"
              :disabled="Boolean(practiceFeedback)"
              title="Correct"
              aria-label="Complete this card"
              @click="completePracticeCard"
            >
              <span>&#10003;</span>
            </button>
            <button
              class="round-nav-btn next"
              :disabled="currentPracticeIndex + 1 >= practiceQueue.length || Boolean(practiceFeedback)"
              title="Next"
              aria-label="Next card"
              @click="goPracticeCard(1)"
            >
              <span aria-hidden="true">&rarr;</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="itemDialogVisible" class="practice-overlay">
      <div class="practice-shell item-shell">
        <button class="close-btn" @click="closeItemDialog">
          x
        </button>
        <div class="item-dialog-head">
          <strong>{{ itemDialogMode === 'edit' ? 'Edit Card' : 'Card Detail' }}</strong>
          <span>Q{{ itemForm.quadrantType }} | Review {{ itemForm.reviewCount }} | Score {{ itemForm.difficultyScore || '-' }}</span>
        </div>
        <div class="item-dialog-body">
          <label>Front</label>
          <textarea v-model="itemForm.frontContent" :readonly="itemDialogMode !== 'edit'" class="practice-answer-input item-textarea" />
          <label>Back</label>
          <textarea v-model="itemForm.backContent" :readonly="itemDialogMode !== 'edit'" class="practice-answer-input item-textarea" />
          <label>Quadrant</label>
          <select v-model="itemForm.quadrantType" :disabled="itemDialogMode !== 'edit'" class="glass-input item-select">
            <option v-for="quadrant in quadrants" :key="quadrant.type" :value="quadrant.type">
              {{ quadrant.title }}
            </option>
          </select>
          <label>Last Answer</label>
          <textarea v-model="itemForm.lastUserAnswer" readonly class="practice-answer-input item-textarea" />
        </div>
        <div v-if="itemDialogMode === 'edit'" class="item-dialog-actions">
          <button class="primary-btn" @click="saveItemDialog">
            Save
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.flashcard-page {
  min-height: 100%;
  padding: 20px;
  color: #172033;
  background:
    radial-gradient(circle at 15% 12%, rgba(239, 68, 68, 0.18), transparent 28%),
    radial-gradient(circle at 86% 18%, rgba(34, 197, 94, 0.16), transparent 26%),
    linear-gradient(135deg, #f8fafc 0%, #e0f2fe 45%, #fef3c7 100%);
}

.flashcard-toolbar,
.inject-panel,
.kanban-board {
  display: grid;
  gap: 14px;
}

.flashcard-toolbar {
  grid-template-columns: minmax(180px, 240px) minmax(260px, 1fr) minmax(320px, auto) auto;
  align-items: end;
  margin-bottom: 16px;
}

.toolbar-block,
.inject-card,
.quadrant-column,
.practice-shell {
  border: 1px solid rgba(255, 255, 255, 0.68);
  background: rgba(255, 255, 255, 0.48);
  backdrop-filter: blur(18px);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.12);
}

.toolbar-block {
  padding: 12px;
  border-radius: 8px;
}

.toolbar-block label {
  display: block;
  margin-bottom: 8px;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.grow {
  min-width: 0;
}

.inline-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.glass-input,
.json-input {
  width: 100%;
  min-height: 38px;
  border: 1px solid rgba(100, 116, 139, 0.24);
  border-radius: 8px;
  padding: 8px 10px;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.64);
  outline: none;
}

.compact {
  max-width: 160px;
}

.file-input {
  padding-top: 7px;
}

.primary-btn,
.secondary-btn,
.practice-btn,
.score-btn,
.close-btn {
  min-height: 38px;
  border: 0;
  border-radius: 8px;
  padding: 0 16px;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn,
.practice-btn {
  background: #2563eb;
}

.secondary-btn {
  background: #0f766e;
}

.practice-btn {
  min-width: 116px;
}

.practice-config {
  min-width: 320px;
}

.practice-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.option-chip,
.move-btn,
.round-nav-btn,
.judge-pill,
.wrong-btn,
.right-btn {
  min-height: 34px;
  border: 1px solid rgba(100, 116, 139, 0.26);
  border-radius: 8px;
  padding: 0 12px;
  color: #334155;
  background: rgba(255, 255, 255, 0.64);
  font-weight: 800;
  cursor: pointer;
}

.option-chip.active {
  border-color: var(--accent-color, #2563eb);
  color: #fff;
  background: var(--accent-color, #2563eb);
}

.order-select {
  width: 112px;
  min-height: 34px;
}

.inject-panel {
  grid-template-columns: minmax(260px, 420px) 1fr;
  margin-bottom: 16px;
}

.inject-card {
  border-radius: 8px;
  padding: 14px;
}

.inject-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.inject-header span {
  font-weight: 800;
}

.inject-header small {
  color: #64748b;
}

.json-input {
  min-height: 88px;
  margin-bottom: 10px;
  resize: vertical;
}

.kanban-board {
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  align-items: start;
}

.quadrant-column {
  display: flex;
  flex-direction: column;
  height: clamp(360px, calc(100vh - 340px), 560px);
  min-height: 360px;
  border-radius: 8px;
  padding: 14px;
  border-top: 4px solid var(--accent-color);
  overflow: hidden;
}

.quadrant-header {
  flex: none;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.quadrant-header h3 {
  margin: 0;
  color: var(--accent-color);
  font-size: 17px;
}

.quadrant-header p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.quadrant-header strong {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: var(--accent-color);
}

.card-list {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.card-list::-webkit-scrollbar {
  width: 6px;
}

.card-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(100, 116, 139, 0.38);
}

.flashcard-tile {
  flex: none;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-left: 4px solid var(--accent-color);
  border-radius: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.72);
  cursor: grab;
}

.tile-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.tile-toolbar {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.18s ease;
  flex: none;
}

.flashcard-tile:hover .tile-toolbar,
.flashcard-tile:focus-within .tile-toolbar {
  opacity: 1;
}

.tile-icon-btn {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border: 1px solid rgba(148, 163, 184, 0.34);
  border-radius: 6px;
  padding: 0;
  color: #334155;
  background: rgba(255, 255, 255, 0.86);
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.tile-icon-btn.danger {
  color: #dc2626;
}

.flashcard-tile p {
  display: -webkit-box;
  margin: 0 0 12px;
  overflow: hidden;
  color: #0f172a;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}

.flashcard-tile span {
  color: #64748b;
  font-size: 12px;
}

.item-shell {
  width: min(760px, calc(100vw - 32px));
}

.item-dialog-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  color: #0f172a;
}

.item-dialog-head strong {
  font-size: 18px;
}

.item-dialog-head span {
  color: #64748b;
  font-size: 12px;
  font-weight: 800;
}

.item-dialog-body {
  display: grid;
  gap: 8px;
}

.item-dialog-body label {
  color: #475569;
  font-size: 12px;
  font-weight: 800;
}

.item-textarea {
  min-height: 88px;
}

.item-textarea[readonly],
.item-select:disabled {
  color: #334155;
  background: rgba(255, 255, 255, 0.46);
  cursor: default;
}

.item-select {
  max-width: 260px;
}

.item-dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.practice-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  background: rgba(15, 23, 42, 0.42);
}

.practice-shell {
  position: relative;
  width: min(820px, calc(100vw - 32px));
  border-radius: 8px;
  padding: 62px 26px 26px;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 5;
  width: 36px;
  padding: 0;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.78);
}

.practice-progress {
  position: absolute;
  top: 14px;
  left: 18px;
  z-index: 2;
  color: #475569;
  font-weight: 800;
  pointer-events: none;
}

.feedback-status {
  position: absolute;
  top: 12px;
  left: 50%;
  z-index: 4;
  font-size: 14px;
  font-weight: 900;
  transform: translateX(-50%);
  pointer-events: none;
}

.feedback-status.known {
  color: #16a34a;
}

.feedback-status.retry {
  color: #dc2626;
}

.flip-scene {
  position: relative;
  height: 320px;
  perspective: 1200px;
}

.flip-card {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 0.45s ease;
  cursor: pointer;
}

.flip-card.flipped {
  transform: rotateY(180deg);
}

.flip-face {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 8px;
  padding: 30px;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.72);
  backface-visibility: hidden;
  overflow-y: auto;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.45;
  text-align: center;
  white-space: pre-wrap;
}

.feedback-card {
  position: absolute;
  inset: -10px;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 3px solid transparent;
  border-radius: 22px;
  pointer-events: none;
  transform-origin: center;
}

.feedback-card span {
  font-size: clamp(28px, 5vw, 44px);
  font-weight: 900;
  text-shadow: 0 2px 18px rgba(255, 255, 255, 0.78);
}

.feedback-card.known {
  border-color: rgba(34, 197, 94, 0.24);
  color: #16a34a;
  background: linear-gradient(135deg, rgba(22, 163, 74, 0.32), rgba(220, 252, 231, 0.72));
  animation: knownFeedback 0.65s ease both;
}

.feedback-card.retry {
  border-color: rgba(239, 68, 68, 0.24);
  color: #dc2626;
  background: linear-gradient(135deg, rgba(254, 226, 226, 0.82), rgba(239, 68, 68, 0.22));
  animation: retryFeedback 0.65s ease both;
}

.back {
  transform: rotateY(180deg);
}

.answer-compare {
  display: grid;
  width: 100%;
  gap: 14px;
  font-size: 15px;
  font-weight: 500;
  text-align: left;
}

.answer-block {
  border-radius: 8px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.64);
}

.answer-block span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 800;
}

.answer-block p {
  min-height: 44px;
  margin: 0;
  white-space: pre-wrap;
}

.user-answer {
  border-left: 4px solid #3b82f6;
  color: #1d4ed8;
}

.standard-answer {
  border-left: 4px solid #22c55e;
  color: #166534;
}

.answer-input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  margin-top: 14px;
}

.practice-answer-input {
  min-height: 86px;
  max-height: 160px;
  border: 1px solid rgba(100, 116, 139, 0.24);
  border-radius: 8px;
  padding: 10px 12px;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.68);
  outline: none;
  resize: vertical;
}

.answer-save-btn {
  width: 46px;
  padding: 0;
  align-self: start;
}

.score-panel {
  display: flex;
  width: min(420px, 100%);
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 14px auto 0;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 999px;
  padding: 8px 10px 8px 16px;
  background: rgba(255, 255, 255, 0.56);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.62);
}

.score-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #475569;
  font-size: 12px;
  font-weight: 800;
}

.score-summary strong {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: #334155;
  font-size: 15px;
}

.score-options {
  display: flex;
  gap: 8px;
}

.score-btn {
  width: 38px;
  height: 38px;
  min-height: 38px;
  border: 1px solid rgba(51, 65, 85, 0.12);
  border-radius: 12px;
  padding: 0;
  color: #334155;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
  font-weight: 900;
}

.score-btn:hover,
.score-btn.active {
  border-color: #2563eb;
  color: #fff;
  background: #2563eb;
  transform: translateY(-1px);
}

.practice-control-bar {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  align-items: center;
  margin-top: 14px;
}

.move-actions,
.judge-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.move-actions {
  justify-content: flex-start;
}

.judge-actions {
  justify-content: center;
  gap: 26px;
}

.current-quadrant-badge {
  display: inline-flex;
  min-height: 34px;
  align-items: center;
  border: 1px solid var(--accent-color);
  border-radius: 8px;
  padding: 0 12px;
  color: #fff;
  background: var(--accent-color);
  font-size: 12px;
  font-weight: 900;
}

.move-btn {
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.move-btn:hover,
.move-btn.active {
  color: #fff;
  background: var(--accent-color);
}

.round-nav-btn {
  display: grid;
  width: 58px;
  height: 58px;
  min-height: 58px;
  place-items: center;
  border: 1px solid rgba(59, 91, 255, 0.9);
  border-radius: 50%;
  color: #3b5bff;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  font-size: 24px;
  font-weight: 900;
}

.round-nav-btn span {
  display: block;
  font-family: Arial, Helvetica, sans-serif;
  font-size: 23px;
  font-weight: 900;
  line-height: 1;
  text-shadow: 0.45px 0 0 currentColor, -0.45px 0 0 currentColor;
}

.round-nav-btn.next {
  border-color: rgba(59, 91, 255, 0.9);
}

.round-nav-btn:disabled {
  cursor: not-allowed;
  opacity: 0.42;
}

.judge-pill {
  display: inline-flex;
  min-width: 78px;
  height: 58px;
  min-height: 58px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
  font-size: 20px;
}

.judge-pill:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.wrong-btn {
  border-color: rgba(239, 68, 68, 0.18);
  color: #ef4444;
  background: rgba(254, 242, 242, 0.96);
}

.right-btn {
  border-color: rgba(34, 197, 94, 0.18);
  color: #16a34a;
  background: rgba(240, 253, 244, 0.96);
}

@keyframes knownFeedback {
  0% {
    opacity: 0;
    border-color: rgba(22, 101, 52, 0.56);
    color: #047857;
    background: linear-gradient(135deg, rgba(22, 163, 74, 0.58), rgba(187, 247, 208, 0.86));
    text-shadow: 0 2px 16px rgba(22, 101, 52, 0.34);
    transform: translateY(18px) scale(0.94) rotate(0deg);
  }

  42% {
    opacity: 1;
    transform: translateY(-8px) scale(1.035) rotate(-4deg);
  }

  100% {
    opacity: 0;
    transform: translateY(-22px) scale(1.06) rotate(-7deg);
  }
}

@keyframes retryFeedback {
  0% {
    opacity: 0;
    border-color: rgba(153, 27, 27, 0.56);
    color: #b91c1c;
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.5), rgba(254, 202, 202, 0.9));
    text-shadow: 0 2px 16px rgba(153, 27, 27, 0.3);
    transform: translateY(18px) scale(0.94) rotate(0deg);
  }

  42% {
    opacity: 1;
    transform: translateY(-8px) scale(1.035) rotate(4deg);
  }

  100% {
    opacity: 0;
    transform: translateY(-22px) scale(1.06) rotate(7deg);
  }
}

@media (max-width: 1100px) {
  .flashcard-toolbar,
  .inject-panel,
  .kanban-board {
    grid-template-columns: 1fr 1fr;
  }

  .practice-btn {
    grid-column: span 2;
  }

  .practice-config {
    min-width: 0;
  }
}

@media (max-width: 720px) {
  .flashcard-page {
    padding: 12px;
  }

  .flashcard-toolbar,
  .inject-panel,
  .kanban-board,
  .inline-controls {
    grid-template-columns: 1fr;
  }

  .inline-controls {
    display: grid;
  }

  .practice-btn {
    grid-column: auto;
  }

  .practice-options,
  .practice-control-bar,
  .score-panel,
  .answer-input-row {
    grid-template-columns: 1fr;
  }

  .practice-control-bar {
    display: grid;
  }

  .judge-actions {
    justify-content: space-between;
  }

  .quadrant-column {
    height: 320px;
    min-height: 280px;
  }

  .flip-scene {
    height: 300px;
  }

  .judge-actions {
    gap: 12px;
  }

  .round-nav-btn,
  .judge-pill {
    width: 48px;
    height: 48px;
    min-width: 48px;
    min-height: 48px;
  }

  .tile-toolbar {
    opacity: 1;
  }

  .score-panel {
    display: grid;
    border-radius: 8px;
  }

  .score-options {
    justify-content: space-between;
  }
}
</style>
