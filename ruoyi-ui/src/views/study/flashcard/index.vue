<template>
  <div class="app-container flashcard-page">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-row">
        <div class="toolbar-main">
          <el-form :inline="true" size="small" label-width="72px">
            <el-form-item label="当前卡组">
              <el-select v-model="selectedBookId" placeholder="请选择卡组" clearable filterable style="width: 260px" @change="handleBookChange">
                <el-option v-for="book in books" :key="book.bookId" :label="book.bookName" :value="book.bookId" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="openBookDialog('add')" v-hasPermi="['study:flashcard:add']">新建卡组</el-button>
              <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="!selectedBook" @click="openBookDialog('edit')" v-hasPermi="['study:flashcard:edit']">编辑卡组</el-button>
              <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="!selectedBook" @click="handleDeleteBook" v-hasPermi="['study:flashcard:remove']">删除卡组</el-button>
              <el-button icon="el-icon-refresh" size="mini" @click="loadBooks">刷新</el-button>
            </el-form-item>
          </el-form>
          <div class="book-description">{{ selectedBook ? (selectedBook.bookDescription || '当前卡组暂无描述') : '请选择卡组后开始学习' }}</div>
        </div>
        <div class="toolbar-actions">
          <el-button type="success" plain icon="el-icon-rank" size="mini" :disabled="selectedItemIds.length === 0" @click="openBatchMoveDialog" v-hasPermi="['study:flashcard:edit']">&#25209;&#37327;&#31227;&#21160;</el-button>
          <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="selectedItemIds.length === 0" @click="handleBatchDeleteItems" v-hasPermi="['study:flashcard:remove']">&#25209;&#37327;&#21024;&#38500;</el-button>
          <el-button type="primary" icon="el-icon-plus" size="mini" :disabled="!selectedBookId" @click="openItemDialog('add')" v-hasPermi="['study:flashcard:add']">新增卡片</el-button>
          <el-button type="warning" icon="el-icon-reading" size="mini" :disabled="!canStartPractice" @click="startPractice" v-hasPermi="['study:flashcard:practice']">开始练习</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="import-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="import-card">
          <div slot="header" class="card-header">
            <span>CSV 导入</span>
          </div>
          <el-form size="small" label-width="88px">
            <el-form-item label="导入象限">
              <el-select v-model="csvQuadrantType" style="width: 100%">
                <el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" />
              </el-select>
            </el-form-item>
            <el-form-item label="CSV 文件">
              <input ref="csvInputRef" type="file" accept=".csv,text/csv" @change="handleCsvFileChange" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="mini" :disabled="!selectedBookId || !csvFile" @click="handleImportCsv" v-hasPermi="['study:flashcard:import']">开始导入</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="import-card">
          <div slot="header" class="card-header">
            <span>JSON 批量注入</span>
          </div>
          <el-form size="small" label-width="88px">
            <el-form-item label="导入象限">
              <el-select v-model="jsonQuadrantType" style="width: 100%">
                <el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" />
              </el-select>
            </el-form-item>
            <el-form-item label="JSON 内容">
              <el-input v-model="jsonText" type="textarea" :rows="6" placeholder='请输入 JSON 数组，例如 [{"frontContent":"题目","backContent":"答案"}]' />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="mini" :disabled="!selectedBookId || !jsonText.trim()" @click="handleImportJson" v-hasPermi="['study:flashcard:import']">批量注入</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="practice-card">
      <div class="practice-toolbar">
        <div class="practice-toolbar-title">练习配置</div>
        <div class="practice-toolbar-controls">
          <el-checkbox-group v-model="practiceQuadrants" size="mini">
            <el-checkbox-button v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.type">Q{{ quadrant.type }}</el-checkbox-button>
          </el-checkbox-group>
          <el-radio-group v-model="practiceOrderMode" size="mini">
            <el-radio-button label="sequence">顺序</el-radio-button>
            <el-radio-button label="random">随机</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="quadrant-row">
      <el-col v-for="quadrant in quadrants" :key="quadrant.type" :xs="24" :sm="12" :lg="6">
        <div class="quadrant-panel" :class="{ 'is-drag-over': dragOverQuadrantType === quadrant.type }" :style="{ borderTopColor: quadrant.accent }">
          <div class="quadrant-head">
            <strong>{{ quadrant.title }}</strong>
            <div class="quadrant-head-actions">
              <span>{{ groupedItems[quadrant.type] ? groupedItems[quadrant.type].length : 0 }} &#24352;</span>
              <el-button type="text" size="mini" :disabled="!groupedItems[quadrant.type] || groupedItems[quadrant.type].length === 0" @click.stop="toggleQuadrantSelection(quadrant.type)">{{ isQuadrantAllSelected(quadrant.type) ? '&#21462;&#28040;' : '&#20840;&#36873;' }}</el-button>
            </div>
          </div>
          <div class="quadrant-subtitle">{{ quadrant.subtitle }}</div>
          <div class="quadrant-body" @dragover.prevent="handleQuadrantDragOver($event, quadrant.type)" @drop.prevent="handleDrop(quadrant.type)">
            <div v-if="isQuadrantEmpty(quadrant.type) && !isDraggingOverQuadrant(quadrant.type)" class="empty-state">&#24403;&#21069;&#35937;&#38480;&#26242;&#26080;&#21345;&#29255;</div>
            <transition-group v-else name="flashcard-drag" tag="div" class="card-list">
              <div v-for="item in groupedItems[quadrant.type]" :key="item.itemId" class="flashcard-tile" :class="{ selected: isItemSelected(item.itemId), 'is-dragging': isDraggingItem(item.itemId), 'is-drop-preview': isDropPreviewItem(item, quadrant.type) }" draggable="true" @dragstart="handleDragStart($event, item)" @dragenter.prevent.stop="handleDragEnter(quadrant.type, item)" @dragover.prevent.stop="handleItemDragOver($event, quadrant.type, item)" @dragend="handleDragEnd" @drop.stop="handleDrop(quadrant.type, item)">
                <div class="tile-top">
                  <div class="tile-title-wrap">
                    <el-checkbox class="tile-check" :value="isItemSelected(item.itemId)" @change="checked => toggleItemSelection(item.itemId, checked)" @click.native.stop />
                    <div class="tile-title">{{ truncateText(item.frontContent, 48) }}</div>
                  </div>
                  <div class="tile-actions">
                    <el-button type="text" size="mini" @click.stop="openItemDialog('view', item)">查看</el-button>
                    <el-button type="text" size="mini" @click.stop="openItemDialog('edit', item)" v-hasPermi="['study:flashcard:edit']">编辑</el-button>
                    <el-button type="text" size="mini" @click.stop="handleDeleteItem(item)" v-hasPermi="['study:flashcard:remove']">删除</el-button>
                  </div>
                </div>
                <div class="tile-meta">
                  <span>复习 {{ item.reviewCount || 0 }}</span>
                  <span>评分 {{ item.difficultyScore || '-' }}</span>
                </div>
                <div class="tile-answer">{{ truncateText(item.lastUserAnswer || '暂无最近答案', 38) }}</div>
              </div>
            </transition-group>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog :title="bookDialogMode === 'add' ? '新建卡组' : '编辑卡组'" :visible.sync="bookDialogOpen" width="520px" append-to-body>
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="88px">
        <el-form-item label="卡组名称" prop="bookName">
          <el-input v-model="bookForm.bookName" maxlength="100" />
        </el-form-item>
        <el-form-item label="卡组描述" prop="bookDescription">
          <el-input v-model="bookForm.bookDescription" type="textarea" :rows="4" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitBookForm">确 定</el-button>
        <el-button @click="bookDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="itemDialogTitle" :visible.sync="itemDialogOpen" width="680px" append-to-body>
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="88px">
        <el-form-item label="正面内容" prop="frontContent">
          <el-input v-model="itemForm.frontContent" type="textarea" :rows="4" :readonly="itemDialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="背面内容" prop="backContent">
          <el-input v-model="itemForm.backContent" type="textarea" :rows="4" :readonly="itemDialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="所属象限" prop="quadrantType">
          <el-select v-model="itemForm.quadrantType" style="width: 100%" :disabled="itemDialogMode === 'view'">
            <el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" />
          </el-select>
        </el-form-item>
        <el-row v-if="itemDialogMode !== 'add'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="复习次数">
              <el-input :value="itemForm.reviewCount || 0" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前评分">
              <el-input :value="itemForm.difficultyScore || '-'" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="itemDialogMode !== 'add'" label="最近答案">
          <el-input :value="itemForm.lastUserAnswer || '暂无最近答案'" type="textarea" :rows="3" readonly />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button v-if="itemDialogMode !== 'view'" type="primary" @click="submitItemForm">确 定</el-button>
        <el-button @click="itemDialogOpen = false">{{ itemDialogMode === 'view' ? '关 闭' : '取 消' }}</el-button>
      </div>
    </el-dialog>


    <el-dialog :title="'\u6279\u91cf\u79fb\u52a8\u8c61\u9650'" :visible.sync="batchMoveDialogOpen" width="420px" append-to-body>
      <el-form size="small" label-width="88px">
        <el-form-item :label="'\u76ee\u6807\u8c61\u9650'">
          <el-select v-model="batchMoveQuadrantType" style="width: 100%">
            <el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" :disabled="!batchMoveQuadrantType" @click="handleBatchMoveItems">&#30830; &#23450;</el-button>
        <el-button @click="batchMoveDialogOpen = false">&#21462; &#28040;</el-button>
      </div>
    </el-dialog>
    <el-dialog title="抽记卡练习" :visible.sync="practiceOpen" width="980px" append-to-body custom-class="practice-dialog" @close="handleClosePractice">
      <div v-if="currentPracticeItem" class="practice-shell practice-shell-modern">
        <div class="practice-stage">
          <div class="practice-stage-meta">
            <span class="practice-progress">{{ currentPracticeIndex + 1 }} / {{ practiceQueue.length }}</span>
            <span class="practice-status practice-status-center" :class="{ success: practiceFeedback === 'known', danger: practiceFeedback === 'retry' }">
              {{ practiceStatusText }}
            </span>
            <span class="practice-guide">{{ practiceGuideText }}</span>
          </div>
          <div
            class="practice-card-face practice-card-face-modern"
            :class="{ flipped: practiceFlipped, known: practiceFeedback === 'known', retry: practiceFeedback === 'retry' }"
            @click="togglePracticeFlip"
          >
            <div v-if="practiceFeedback" class="practice-feedback-mask" :class="practiceFeedback">
              <span>{{ practiceFeedback === 'known' ? '√' : '×' }}</span>
            </div>
            <div class="practice-face">
              <div class="practice-face-content practice-face-question">
                <template v-if="!practiceFlipped">
                  {{ currentPracticeItem.frontContent }}
                </template>
                <div v-else class="practice-answer-compare">
                  <div class="practice-answer-block user-answer">
                    <span>我的答案</span>
                    <p>{{ practiceAnswerDraft || currentPracticeItem.lastUserAnswer || '暂无输入答案' }}</p>
                  </div>
                  <div class="practice-answer-block standard-answer">
                    <span>标准答案</span>
                    <p>{{ currentPracticeItem.backContent }}</p>
                  </div>
                </div>
              </div>
              <div class="practice-face-hint">{{ practiceFlipped ? '空格可切回题面' : '查看答案 / 空格翻面' }}</div>
            </div>
          </div>
        </div>

        <div class="practice-answer-row">
          <el-input
            v-model="practiceAnswerDraft"
            type="textarea"
            :rows="3"
            resize="none"
            class="practice-answer-input"
            placeholder="输入你的答案，翻面后可对照查看"
          />
          <el-button class="practice-answer-save" type="primary" plain @click="saveCurrentAnswer">保存答案</el-button>
        </div>

        <div class="practice-judge-row">
          <button
            class="round-nav-btn"
            :disabled="currentPracticeIndex <= 0 || !!practiceFeedback"
            title="上一题"
            @click="goToPreviousPractice"
          >
            <span aria-hidden="true">&larr;</span>
          </button>
          <button
            class="judge-pill wrong-btn"
            :disabled="!!practiceFeedback"
            title="答错，加入重试"
            @click="retryCurrentPractice"
          >
            <span aria-hidden="true">&times;</span>
            <em>{{ practiceRetryCount }}</em>
          </button>
          <button
            class="judge-pill right-btn"
            :disabled="!!practiceFeedback"
            title="答对，进入下一题"
            @click="completePracticeCard"
          >
            <span aria-hidden="true">&#10003;</span>
            <em>{{ practiceKnownCount }}</em>
          </button>
          <button
            class="round-nav-btn next"
            :disabled="currentPracticeIndex >= practiceQueue.length - 1 || !!practiceFeedback"
            title="下一题"
            @click="goToNextPractice"
          >
            <span aria-hidden="true">&rarr;</span>
          </button>
        </div>

        <div class="practice-secondary-panel">
          <div class="practice-secondary-group">
            <span class="action-label">评分</span>
            <el-button
              v-for="score in scoreOptions"
              :key="score"
              size="mini"
              :type="currentPracticeItem.difficultyScore === score ? 'primary' : 'default'"
              plain
              :title="'快捷键 ' + score"
              @click="scoreCurrentItem(score)"
            >
              {{ score }}
            </el-button>
          </div>
          <div class="practice-secondary-group">
            <span class="action-label">移动到</span>
            <el-button
              v-for="quadrant in quadrants"
              :key="quadrant.type"
              size="mini"
              :type="currentPracticeItem.quadrantType === quadrant.type ? 'success' : 'default'"
              plain
              @click="movePracticeItem(quadrant.type)"
            >
              Q{{ quadrant.type }}
            </el-button>
          </div>
          <div class="practice-secondary-group practice-card-meta">
            <span>当前卡组 {{ selectedBook ? selectedBook.bookName : '-' }}</span>
            <span>当前象限 Q{{ currentPracticeItem.quadrantType }}</span>
            <span>复习 {{ currentPracticeItem.reviewCount || 0 }}</span>
            <span>评分 {{ currentPracticeItem.difficultyScore || '-' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">没有可练习的卡片</div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listBook,
  addBook,
  updateBook,
  delBook,
  listItem,
  addItem,
  updateItem,
  delItem,
  moveItem,
  scoreItem,
  saveItemAnswer,
  importJsonItems,
  importCsvItems
} from "@/api/study/flashcard"

export default {
  name: "StudyFlashcard",
  data() {
    return {
      quadrants: [
        { type: 1, title: "Q1 重要 / 困难", subtitle: "高价值，当前不熟", accent: "#f56c6c" },
        { type: 2, title: "Q2 重要 / 熟悉", subtitle: "高价值，持续巩固", accent: "#409eff" },
        { type: 3, title: "Q3 次要 / 困难", subtitle: "低优先级，当前不熟", accent: "#e6a23c" },
        { type: 4, title: "Q4 次要 / 熟悉", subtitle: "低优先级，可归档", accent: "#67c23a" }
      ],
      books: [],
      items: [],
      selectedBookId: undefined,
      bookDialogOpen: false,
      bookDialogMode: "add",
      bookForm: {
        bookId: undefined,
        bookName: "",
        bookDescription: ""
      },
      bookRules: {
        bookName: [
          { required: true, message: "卡组名称不能为空", trigger: "blur" }
        ]
      },
      itemDialogOpen: false,
      itemDialogMode: "add",
      batchMoveDialogOpen: false,
      batchMoveQuadrantType: undefined,
      itemForm: {
        itemId: undefined,
        bookId: undefined,
        frontContent: "",
        backContent: "",
        quadrantType: 1,
        reviewCount: 0,
        difficultyScore: undefined,
        lastUserAnswer: ""
      },
      itemRules: {
        frontContent: [
          { required: true, message: "正面内容不能为空", trigger: "blur" }
        ],
        backContent: [
          { required: true, message: "背面内容不能为空", trigger: "blur" }
        ],
        quadrantType: [
          { required: true, message: "请选择所属象限", trigger: "change" }
        ]
      },
      draggedItemId: undefined,
      dragOverItemId: undefined,
      dragOverQuadrantType: undefined,
      dragInsertAfter: false,
      dragImageEl: null,
      dragScrollFrame: null,
      dragScrollContainer: null,
      dragScrollSpeed: 0,
      lastDropTargetItem: null,
      selectedItemIds: [],
      itemOrderMap: {},
      csvFile: null,
      csvQuadrantType: 1,
      jsonQuadrantType: 1,
      jsonText: "",
      practiceOpen: false,
      practiceQueue: [],
      currentPracticeIndex: 0,
      practiceAnswerDraft: "",
      practiceFlipped: false,
      practiceFeedback: "",
      practiceQuadrants: [1, 2, 3, 4],
      practiceOrderMode: "sequence",
      scoreOptions: [1, 2, 3, 4, 5],
      practiceFeedbackTimer: null
    }
  },
  computed: {
    selectedBook() {
      return this.books.find(book => book.bookId === this.selectedBookId)
    },
    groupedItems() {
      const result = {}
      this.quadrants.forEach(quadrant => {
        result[quadrant.type] = this.getPreviewItemsByQuadrant(quadrant.type)
      })
      return result
    },
    canStartPractice() {
      return !!this.selectedBookId && this.items.some(item => this.practiceQuadrants.indexOf(item.quadrantType) !== -1)
    },
    currentPracticeItem() {
      return this.practiceQueue[this.currentPracticeIndex] || null
    },
    practiceKnownCount() {
      return this.practiceQueue.filter(item => item.practiceResult === "known").length
    },
    practiceRetryCount() {
      return this.practiceQueue.filter(item => item.practiceResult === "retry").length
    },
    practiceStatusText() {
      if (this.practiceFeedback === "known") {
        return "明白了"
      }
      if (this.practiceFeedback === "retry") {
        return "不明白"
      }
      return this.currentPracticeItem && this.currentPracticeItem.practiceResult === "known" ? "明白了" : (this.currentPracticeItem && this.currentPracticeItem.practiceResult === "retry" ? "不明白" : "")
    },
    practiceGuideText() {
      return this.practiceFlipped ? "已翻面" : "请先作答"
    },
    itemDialogTitle() {
      if (this.itemDialogMode === "add") {
        return "新增卡片"
      }
      return this.itemDialogMode === "edit" ? "编辑卡片" : "卡片详情"
    }
  },
  created() {
    this.loadBooks()
  },
  mounted() {
    window.addEventListener("keydown", this.handlePracticeHotkey)
  },
  beforeDestroy() {
    window.removeEventListener("keydown", this.handlePracticeHotkey)
    this.clearPracticeFeedbackTimer()
    this.stopDragAutoScroll()
  },
  methods: {
    loadBooks() {
      listBook().then(response => {
        this.books = response.data || []
        if (!this.selectedBookId && this.books.length > 0) {
          this.selectedBookId = this.books[0].bookId
        }
        if (this.selectedBookId) {
          const exists = this.books.some(book => book.bookId === this.selectedBookId)
          if (!exists) {
            this.selectedBookId = this.books.length > 0 ? this.books[0].bookId : undefined
          }
        }
        return this.loadItems()
      })
    },
    loadItems() {
      if (!this.selectedBookId) {
        this.items = []
        this.selectedItemIds = []
        this.itemOrderMap = {}
        return Promise.resolve()
      }
      this.loadItemOrder()
      return listItem({ bookId: this.selectedBookId }).then(response => {
        this.items = response.data || []
        this.selectedItemIds = this.selectedItemIds.filter(itemId => this.items.some(item => item.itemId === itemId))
      })
    },
    handleBookChange() {
      if (this.practiceOpen) {
        this.practiceOpen = false
        this.handleClosePractice()
      }
      this.selectedItemIds = []
      this.loadItems()
    },
    openBookDialog(mode) {
      this.bookDialogMode = mode
      if (mode === "edit" && this.selectedBook) {
        this.bookForm = {
          bookId: this.selectedBook.bookId,
          bookName: this.selectedBook.bookName,
          bookDescription: this.selectedBook.bookDescription || ""
        }
      } else {
        this.bookForm = {
          bookId: undefined,
          bookName: "",
          bookDescription: ""
        }
      }
      this.bookDialogOpen = true
      this.$nextTick(() => {
        if (this.$refs.bookFormRef) {
          this.$refs.bookFormRef.clearValidate()
        }
      })
    },
    submitBookForm() {
      this.$refs.bookFormRef.validate(valid => {
        if (!valid) {
          return
        }
        const request = this.bookDialogMode === "edit" ? updateBook(this.bookForm) : addBook(this.bookForm)
        request.then(() => {
          this.$modal.msgSuccess(this.bookDialogMode === "edit" ? "卡组修改成功" : "卡组创建成功")
          this.bookDialogOpen = false
          this.loadBooks()
        })
      })
    },
    handleDeleteBook() {
      if (!this.selectedBook) {
        return
      }
      this.$modal.confirm('是否确认删除卡组“' + this.selectedBook.bookName + '”？删除后其下卡片会一并移除。').then(() => {
        return delBook(this.selectedBook.bookId)
      }).then(() => {
        this.$modal.msgSuccess("卡组删除成功")
        this.selectedBookId = undefined
        this.loadBooks()
      }).catch(() => {})
    },
    openItemDialog(mode, item) {
      this.itemDialogMode = mode
      if (mode === "add") {
        this.itemForm = {
          itemId: undefined,
          bookId: this.selectedBookId,
          frontContent: "",
          backContent: "",
          quadrantType: 1,
          reviewCount: 0,
          difficultyScore: undefined,
          lastUserAnswer: ""
        }
      } else {
        this.itemForm = {
          itemId: item.itemId,
          bookId: item.bookId,
          frontContent: item.frontContent,
          backContent: item.backContent,
          quadrantType: item.quadrantType,
          reviewCount: item.reviewCount || 0,
          difficultyScore: item.difficultyScore,
          lastUserAnswer: item.lastUserAnswer || ""
        }
      }
      this.itemDialogOpen = true
      this.$nextTick(() => {
        if (this.$refs.itemFormRef) {
          this.$refs.itemFormRef.clearValidate()
        }
      })
    },
    submitItemForm() {
      this.$refs.itemFormRef.validate(valid => {
        if (!valid) {
          return
        }
        const payload = {
          itemId: this.itemForm.itemId,
          bookId: this.itemForm.bookId || this.selectedBookId,
          frontContent: this.itemForm.frontContent,
          backContent: this.itemForm.backContent,
          quadrantType: this.itemForm.quadrantType
        }
        const request = this.itemDialogMode === "edit" ? updateItem(payload) : addItem(payload)
        request.then(() => {
          this.$modal.msgSuccess(this.itemDialogMode === "edit" ? "卡片修改成功" : "卡片新增成功")
          this.itemDialogOpen = false
          this.loadItems()
        })
      })
    },
    handleDeleteItem(item) {
      this.$modal.confirm('是否确认删除卡片“' + this.truncateText(item.frontContent, 18) + '”？').then(() => {
        return delItem(item.itemId)
      }).then(() => {
        this.$modal.msgSuccess("卡片删除成功")
        this.removeItemState(item.itemId)
      }).catch(() => {})
    },
    handleDragStart(event, item) {
      this.draggedItemId = item.itemId
      this.dragOverItemId = item.itemId
      this.dragOverQuadrantType = item.quadrantType
      this.dragInsertAfter = false
      this.lastDropTargetItem = item
      if (event && event.dataTransfer) {
        event.dataTransfer.effectAllowed = "move"
        event.dataTransfer.setData("text/plain", String(item.itemId))
        this.setDragImage(event)
      }
    },
    handleBatchDeleteItems() {
      if (this.selectedItemIds.length === 0) {
        return
      }
      const deleteIds = this.selectedItemIds.slice()
      this.$modal.confirm("\u662f\u5426\u786e\u8ba4\u5220\u9664\u9009\u4e2d\u7684 " + deleteIds.length + " \u5f20\u62bd\u8bb0\u5361\uff1f").then(() => {
        return delItem(deleteIds.join(","))
      }).then(() => {
        this.$modal.msgSuccess("\u6279\u91cf\u5220\u9664\u6210\u529f")
        this.removeItemStates(deleteIds)
      }).catch(() => {})
    },
    openBatchMoveDialog() {
      if (this.selectedItemIds.length === 0) {
        return
      }
      this.batchMoveQuadrantType = undefined
      this.batchMoveDialogOpen = true
    },
    handleBatchMoveItems() {
      if (this.selectedItemIds.length === 0 || !this.batchMoveQuadrantType) {
        return
      }
      const moveIds = this.selectedItemIds.slice()
      const targetQuadrantType = this.batchMoveQuadrantType
      const movableItems = moveIds.map(itemId => this.items.find(item => item.itemId === itemId))
        .filter(item => item && item.quadrantType !== targetQuadrantType)
      if (movableItems.length === 0) {
        this.$modal.msgWarning("\u9009\u4e2d\u5361\u7247\u5df2\u5728\u76ee\u6807\u8c61\u9650")
        return
      }
      Promise.all(movableItems.map(item => moveItem({
        itemId: item.itemId,
        quadrantType: targetQuadrantType
      }))).then(responses => {
        responses.forEach(response => {
          this.syncItemState(response.data)
        })
        this.appendMovedItemsToQuadrantTail(movableItems.map(item => item.itemId), targetQuadrantType)
        this.selectedItemIds = []
        this.batchMoveDialogOpen = false
        this.$modal.msgSuccess("\u6279\u91cf\u79fb\u52a8\u6210\u529f")
      })
    },
    handleQuadrantDragOver(event, quadrantType) {
      if (!this.draggedItemId) {
        return
      }
      this.dragOverQuadrantType = quadrantType
      this.handleAutoScroll(event)
      if (this.isDraggingBelowList(event)) {
        this.dragOverItemId = undefined
        this.dragInsertAfter = false
        this.lastDropTargetItem = null
      }
    },
    handleDragEnter(quadrantType, targetItem) {
      if (!this.draggedItemId || !targetItem || targetItem.itemId === this.draggedItemId) {
        return
      }
      this.dragOverQuadrantType = quadrantType
      this.dragOverItemId = targetItem.itemId
      this.dragInsertAfter = false
      this.lastDropTargetItem = targetItem
    },
    handleItemDragOver(event, quadrantType, targetItem) {
      this.handleDragEnter(quadrantType, targetItem)
      this.dragInsertAfter = this.isAfterTargetCard(event)
      this.handleAutoScroll(event)
    },
    handleDragEnd() {
      this.clearDragState()
    },
    handleDrop(quadrantType, targetItem) {
      if (!this.draggedItemId) {
        return
      }
      const draggedItem = this.items.find(item => item.itemId === this.draggedItemId)
      if (!draggedItem) {
        this.clearDragState()
        return
      }
      const targetQuadrantType = quadrantType || this.dragOverQuadrantType || draggedItem.quadrantType
      const rememberedTargetItem = this.lastDropTargetItem && this.lastDropTargetItem.quadrantType === targetQuadrantType ? this.lastDropTargetItem : null
      const dropTargetItem = targetItem && targetItem.itemId !== draggedItem.itemId ? targetItem : rememberedTargetItem
      const finishDrop = updatedItem => {
        const sourceItem = updatedItem || Object.assign({}, draggedItem, { quadrantType: targetQuadrantType })
        this.reorderDraggedItem(sourceItem, targetQuadrantType, dropTargetItem)
        this.clearDragState()
      }
      if (draggedItem.quadrantType !== targetQuadrantType) {
        moveItem({
          itemId: draggedItem.itemId,
          quadrantType: targetQuadrantType
        }).then(response => {
          finishDrop(response.data)
        }).catch(() => {
          this.clearDragState()
        })
        return
      }
      finishDrop(draggedItem)
    },
    handleCsvFileChange(event) {
      this.csvFile = event.target.files[0] || null
    },
    handleImportCsv() {
      if (!this.selectedBookId || !this.csvFile) {
        return
      }
      importCsvItems(this.selectedBookId, this.csvQuadrantType, this.csvFile).then(response => {
        this.showImportResult(response.data, "CSV 导入完成")
        this.loadItems()
      }).finally(() => {
        this.resetCsvInput()
      })
    },
    handleImportJson() {
      if (!this.selectedBookId) {
        return
      }
      let parsed
      try {
        parsed = JSON.parse(this.jsonText)
      } catch (error) {
        this.$modal.msgError("JSON 格式不正确")
        return
      }
      if (!Array.isArray(parsed)) {
        this.$modal.msgError("JSON 内容必须是数组")
        return
      }
      importJsonItems({
        bookId: this.selectedBookId,
        quadrantType: this.jsonQuadrantType,
        items: parsed
      }).then(response => {
        this.showImportResult(response.data, "JSON 注入完成")
        this.jsonText = ""
        this.loadItems()
      })
    },
    showImportResult(result, title) {
      const data = result || {}
      const messages = (data.failureMessages || []).slice(0, 3).join("；")
      const summary = title + "，总数 " + (data.totalCount || 0) + "，成功 " + (data.successCount || 0) + "，失败 " + (data.failureCount || 0)
      if (messages) {
        this.$alert(summary + "。失败示例：" + messages, "导入结果", { confirmButtonText: "知道了" })
      } else {
        this.$modal.msgSuccess(summary)
      }
    },
    startPractice() {
      const sourceItems = this.getPracticeSourceItems()
      if (sourceItems.length === 0) {
        this.$modal.msgWarning("当前没有可练习的卡片")
        return
      }
      const queue = this.practiceOrderMode === "random" ? this.shuffleItems(sourceItems) : sourceItems.slice()
      this.practiceQueue = queue.map(item => Object.assign({}, item))
      this.currentPracticeIndex = 0
      this.practiceAnswerDraft = ""
      this.practiceFlipped = false
      this.practiceFeedback = ""
      this.practiceOpen = true
      this.fillPracticeDraft()
    },
    getPracticeSourceItems() {
      // \u6309\u770b\u677f\u5f53\u524d\u663e\u793a\u987a\u5e8f\u7ec4\u88c5\u7ec3\u4e60\u961f\u5217
      // Build the practice queue from the current board display order
      return this.practiceQuadrants.reduce((result, quadrantType) => {
        const quadrantItems = this.sortItemsForDisplay(this.items.filter(item => item.quadrantType === quadrantType))
        return result.concat(quadrantItems)
      }, [])
    },
    handleClosePractice() {
      this.clearPracticeFeedbackTimer()
      this.practiceQueue = []
      this.currentPracticeIndex = 0
      this.practiceAnswerDraft = ""
      this.practiceFlipped = false
      this.practiceFeedback = ""
    },
    fillPracticeDraft() {
      // 切题时清空作答输入，避免显示历史答案
      // Clear the answer input on card change to avoid showing previous answers
      this.practiceAnswerDraft = ""
      this.practiceFlipped = false
      this.practiceFeedback = ""
    },
    togglePracticeFlip() {
      this.practiceFlipped = !this.practiceFlipped
    },
    clearPracticeFeedbackTimer() {
      if (this.practiceFeedbackTimer) {
        window.clearTimeout(this.practiceFeedbackTimer)
        this.practiceFeedbackTimer = null
      }
    },
    markCurrentPracticeResult(result) {
      if (!this.currentPracticeItem) {
        return
      }
      this.practiceQueue = this.practiceQueue.map((item, index) => {
        if (index !== this.currentPracticeIndex) {
          return item
        }
        return Object.assign({}, item, { practiceResult: result })
      })
    },
    shouldIgnorePracticeHotkey(target) {
      const tagName = target && target.tagName ? target.tagName.toLowerCase() : ""
      return tagName === "input" || tagName === "textarea" || tagName === "select" || (target && target.isContentEditable)
    },
    handlePracticeHotkey(event) {
      if (!this.practiceOpen || this.practiceFeedback || this.shouldIgnorePracticeHotkey(event.target)) {
        return
      }
      const score = Number(event.key)
      if (this.scoreOptions.indexOf(score) !== -1) {
        event.preventDefault()
        this.scoreCurrentItem(score)
      } else if (event.key === "ArrowLeft") {
        event.preventDefault()
        this.goToPreviousPractice()
      } else if (event.key === "ArrowRight") {
        event.preventDefault()
        this.goToNextPractice()
      } else if (event.code === "Space") {
        event.preventDefault()
        this.togglePracticeFlip()
      } else if (event.key === "Escape") {
        event.preventDefault()
        this.practiceOpen = false
      }
    },
    playPracticeFeedback(type, callback) {
      if (this.practiceFeedback) {
        return
      }
      this.practiceFeedback = type
      this.clearPracticeFeedbackTimer()
      // 答题反馈短暂停留后自动切题，沿用旧版练习节奏
      // Keep a short visual feedback pause before auto-advancing, matching the legacy practice rhythm
      this.practiceFeedbackTimer = window.setTimeout(() => {
        this.practiceFeedback = ""
        this.practiceFeedbackTimer = null
        callback()
      }, 650)
    },
    saveCurrentAnswer() {
      if (!this.currentPracticeItem) {
        return
      }
      const submittedAnswer = this.practiceAnswerDraft
      saveItemAnswer({
        itemId: this.currentPracticeItem.itemId,
        lastUserAnswer: submittedAnswer
      }).then(response => {
        this.syncItemState(response.data)
        this.practiceAnswerDraft = submittedAnswer
        this.practiceFlipped = true
        this.$modal.msgSuccess("答案已保存")
      })
    },
    scoreCurrentItem(score) {
      if (!this.currentPracticeItem) {
        return
      }
      scoreItem({
        itemId: this.currentPracticeItem.itemId,
        difficultyScore: score
      }).then(response => {
        this.syncItemState(response.data)
        this.$modal.msgSuccess("评分已保存")
      })
    },
    movePracticeItem(quadrantType) {
      if (!this.currentPracticeItem) {
        return
      }
      moveItem({
        itemId: this.currentPracticeItem.itemId,
        quadrantType: quadrantType
      }).then(response => {
        this.syncItemState(response.data)
        this.$modal.msgSuccess("象限已更新")
      })
    },
    retryCurrentPractice() {
      if (!this.currentPracticeItem || this.practiceFeedback) {
        return
      }
      this.markCurrentPracticeResult("retry")
      // 将答错卡片追加到队尾，保持本轮练习上下文
      // Append the wrong card to the queue tail and keep the current practice context
      this.practiceQueue.push(Object.assign({}, this.currentPracticeItem))
      this.playPracticeFeedback("retry", () => {
        this.advancePracticeCard()
      })
    },
    completePracticeCard() {
      if (!this.currentPracticeItem || this.practiceFeedback) {
        return
      }
      this.markCurrentPracticeResult("known")
      this.playPracticeFeedback("known", () => {
        this.advancePracticeCard()
      })
    },
    advancePracticeCard() {
      if (this.currentPracticeIndex >= this.practiceQueue.length - 1) {
        this.$modal.msgSuccess("本轮练习已完成")
        this.practiceOpen = false
        return
      }
      this.currentPracticeIndex += 1
      this.fillPracticeDraft()
    },
    goToPreviousPractice() {
      if (this.currentPracticeIndex <= 0 || this.practiceFeedback) {
        return
      }
      this.currentPracticeIndex -= 1
      this.fillPracticeDraft()
    },
    goToNextPractice() {
      if (this.practiceFeedback) {
        return
      }
      if (this.currentPracticeIndex >= this.practiceQueue.length - 1) {
        this.$modal.msgSuccess("本轮练习已完成")
        return
      }
      this.currentPracticeIndex += 1
      this.fillPracticeDraft()
    },
    syncItemState(updatedItem) {
      if (!updatedItem || !updatedItem.itemId) {
        return
      }
      // 同步看板和练习队列中的同一张卡，避免前后状态不一致
      // Sync the same card in board state and practice queue to avoid stale state
      this.items = this.items.map(item => item.itemId === updatedItem.itemId ? updatedItem : item)
      this.practiceQueue = this.practiceQueue.map(item => item.itemId === updatedItem.itemId ? Object.assign({}, updatedItem) : item)
    },
    getPreviewItemsByQuadrant(quadrantType) {
      const sourceItems = this.items.filter(item => item.quadrantType === quadrantType)
      const sortedItems = this.sortItemsForDisplay(sourceItems)
      if (!this.draggedItemId || this.dragOverQuadrantType !== quadrantType) {
        return sortedItems
      }
      const draggedItem = this.items.find(item => item.itemId === this.draggedItemId)
      if (!draggedItem) {
        return sortedItems
      }
      const previewItem = Object.assign({}, draggedItem, { quadrantType: quadrantType })
      const previewItems = sortedItems.filter(item => item.itemId !== this.draggedItemId)
      const targetIndex = this.dragOverItemId ? previewItems.findIndex(item => item.itemId === this.dragOverItemId) : -1
      const insertIndex = targetIndex === -1 ? previewItems.length : targetIndex + (this.dragInsertAfter ? 1 : 0)
      previewItems.splice(insertIndex, 0, previewItem)
      return previewItems
    },
    isDraggingItem(itemId) {
      return this.draggedItemId === itemId
    },
    isDropPreviewItem(item, quadrantType) {
      return !!this.draggedItemId && this.draggedItemId === item.itemId && this.dragOverQuadrantType === quadrantType
    },
    isQuadrantEmpty(quadrantType) {
      return !this.items.some(item => item.quadrantType === quadrantType)
    },
    isDraggingOverQuadrant(quadrantType) {
      return !!this.draggedItemId && this.dragOverQuadrantType === quadrantType
    },
    isAfterTargetCard(event) {
      if (!event || !event.currentTarget) {
        return false
      }
      const rect = event.currentTarget.getBoundingClientRect()
      return event.clientY > rect.top + rect.height / 2
    },
    isDraggingBelowList(event) {
      if (!event || !event.currentTarget) {
        return false
      }
      const cardList = event.currentTarget.querySelector(".card-list")
      if (!cardList) {
        return true
      }
      const rect = cardList.getBoundingClientRect()
      return event.clientY > rect.bottom - 8
    },
    setDragImage(event) {
      const sourceEl = event.currentTarget
      if (!sourceEl || !event.dataTransfer) {
        return
      }
      const dragImageEl = sourceEl.cloneNode(true)
      dragImageEl.classList.add("flashcard-drag-image")
      dragImageEl.style.width = sourceEl.offsetWidth + "px"
      dragImageEl.style.position = "fixed"
      dragImageEl.style.top = "-1000px"
      dragImageEl.style.left = "-1000px"
      dragImageEl.style.opacity = "0.9"
      dragImageEl.style.pointerEvents = "none"
      dragImageEl.style.transform = "scale(1.015)"
      dragImageEl.style.boxShadow = "0 18px 36px rgba(15, 23, 42, 0.2)"
      document.body.appendChild(dragImageEl)
      event.dataTransfer.setDragImage(dragImageEl, sourceEl.offsetWidth / 2, Math.min(sourceEl.offsetHeight / 2, 56))
      this.dragImageEl = dragImageEl
    },
    handleAutoScroll(event) {
      if (!this.draggedItemId || !event || !event.currentTarget) {
        return
      }
      const scrollContainer = event.currentTarget.closest(".quadrant-body")
      if (!scrollContainer) {
        return
      }
      const rect = scrollContainer.getBoundingClientRect()
      const edgeSize = 56
      const maxSpeed = 18
      let nextSpeed = 0
      if (event.clientY < rect.top + edgeSize) {
        nextSpeed = -Math.ceil(((rect.top + edgeSize - event.clientY) / edgeSize) * maxSpeed)
      } else if (event.clientY > rect.bottom - edgeSize) {
        nextSpeed = Math.ceil(((event.clientY - (rect.bottom - edgeSize)) / edgeSize) * maxSpeed)
      }
      this.dragScrollContainer = scrollContainer
      this.dragScrollSpeed = nextSpeed
      if (nextSpeed === 0) {
        this.stopDragAutoScroll()
        return
      }
      this.startDragAutoScroll()
    },
    startDragAutoScroll() {
      if (this.dragScrollFrame) {
        return
      }
      const step = () => {
        if (!this.dragScrollContainer || !this.dragScrollSpeed) {
          this.stopDragAutoScroll()
          return
        }
        this.dragScrollContainer.scrollTop += this.dragScrollSpeed
        this.dragScrollFrame = window.requestAnimationFrame(step)
      }
      this.dragScrollFrame = window.requestAnimationFrame(step)
    },
    stopDragAutoScroll() {
      if (this.dragScrollFrame) {
        window.cancelAnimationFrame(this.dragScrollFrame)
      }
      this.dragScrollFrame = null
      this.dragScrollContainer = null
      this.dragScrollSpeed = 0
    },
    clearDragState() {
      if (this.dragImageEl && this.dragImageEl.parentNode) {
        this.dragImageEl.parentNode.removeChild(this.dragImageEl)
      }
      this.stopDragAutoScroll()
      this.draggedItemId = undefined
      this.dragOverItemId = undefined
      this.dragOverQuadrantType = undefined
      this.dragInsertAfter = false
      this.dragImageEl = null
      this.lastDropTargetItem = null
    },
    sortItemsForDisplay(sourceItems) {
      return sourceItems.slice().sort((first, second) => {
        const firstOrder = this.itemOrderMap[first.itemId]
        const secondOrder = this.itemOrderMap[second.itemId]
        if (firstOrder !== undefined || secondOrder !== undefined) {
          return (firstOrder === undefined ? Number.MAX_SAFE_INTEGER : firstOrder) - (secondOrder === undefined ? Number.MAX_SAFE_INTEGER : secondOrder)
        }
        return (first.itemId || 0) - (second.itemId || 0)
      })
    },
    reorderDraggedItem(draggedItem, quadrantType, targetItem) {
      const nextItems = this.items.map(item => item.itemId === draggedItem.itemId ? Object.assign({}, draggedItem, { quadrantType: quadrantType }) : item)
      const quadrantItems = this.sortItemsForDisplay(nextItems.filter(item => item.quadrantType === quadrantType && item.itemId !== draggedItem.itemId))
      const targetIndex = targetItem ? quadrantItems.findIndex(item => item.itemId === targetItem.itemId) : -1
      const insertIndex = targetIndex === -1 ? quadrantItems.length : targetIndex + (this.dragInsertAfter ? 1 : 0)
      quadrantItems.splice(insertIndex, 0, Object.assign({}, draggedItem, { quadrantType: quadrantType }))

      // Save user drag display order
      // Save the display order after the user drags cards
      const nextOrderMap = Object.assign({}, this.itemOrderMap)
      quadrantItems.forEach((item, index) => {
        nextOrderMap[item.itemId] = index + 1
      })
      this.itemOrderMap = nextOrderMap
      this.items = nextItems
      this.saveItemOrder()
    },
    appendMovedItemsToQuadrantTail(itemIds, quadrantType) {
      const targetItems = this.sortItemsForDisplay(this.items.filter(item => item.quadrantType === quadrantType))
      const movedIdMap = itemIds.reduce((result, itemId) => {
        result[itemId] = true
        return result
      }, {})
      const orderedItems = targetItems.filter(item => !movedIdMap[item.itemId])
        .concat(targetItems.filter(item => movedIdMap[item.itemId]))

      // \u5c06\u6279\u91cf\u79fb\u52a8\u7684\u5361\u7247\u653e\u5230\u76ee\u6807\u8c61\u9650\u672b\u5c3e
      // Append batch moved cards to the target quadrant tail
      const nextOrderMap = Object.assign({}, this.itemOrderMap)
      orderedItems.forEach((item, index) => {
        nextOrderMap[item.itemId] = index + 1
      })
      this.itemOrderMap = nextOrderMap
      this.saveItemOrder()
    },
    getItemOrderStorageKey() {
      return "study_flashcard_item_order_" + this.selectedBookId
    },
    loadItemOrder() {
      if (!this.selectedBookId) {
        this.itemOrderMap = {}
        return
      }
      try {
        this.itemOrderMap = JSON.parse(window.localStorage.getItem(this.getItemOrderStorageKey()) || "{}")
      } catch (error) {
        this.itemOrderMap = {}
      }
    },
    saveItemOrder() {
      if (!this.selectedBookId) {
        return
      }
      window.localStorage.setItem(this.getItemOrderStorageKey(), JSON.stringify(this.itemOrderMap))
    },
    isItemSelected(itemId) {
      return this.selectedItemIds.indexOf(itemId) !== -1
    },
    isQuadrantAllSelected(quadrantType) {
      const quadrantItems = this.groupedItems[quadrantType] || []
      return quadrantItems.length > 0 && quadrantItems.every(item => this.isItemSelected(item.itemId))
    },
    toggleQuadrantSelection(quadrantType) {
      const quadrantItems = this.groupedItems[quadrantType] || []
      if (quadrantItems.length === 0) {
        return
      }
      const quadrantItemIds = quadrantItems.map(item => item.itemId)
      if (this.isQuadrantAllSelected(quadrantType)) {
        this.selectedItemIds = this.selectedItemIds.filter(itemId => quadrantItemIds.indexOf(itemId) === -1)
        return
      }
      // \u5168\u9009\u5f53\u524d\u8c61\u9650\u5185\u7684\u5361\u7247
      // Select all cards in the current quadrant
      const nextSelectedIds = this.selectedItemIds.slice()
      quadrantItemIds.forEach(itemId => {
        if (nextSelectedIds.indexOf(itemId) === -1) {
          nextSelectedIds.push(itemId)
        }
      })
      this.selectedItemIds = nextSelectedIds
    },
    toggleItemSelection(itemId, checked) {
      if (checked && !this.isItemSelected(itemId)) {
        this.selectedItemIds = this.selectedItemIds.concat(itemId)
      } else if (!checked) {
        this.selectedItemIds = this.selectedItemIds.filter(selectedId => selectedId !== itemId)
      }
    },
    removeItemState(itemId) {
      this.items = this.items.filter(item => item.itemId !== itemId)
      this.practiceQueue = this.practiceQueue.filter(item => item.itemId !== itemId)
      this.selectedItemIds = this.selectedItemIds.filter(selectedId => selectedId !== itemId)
      if (this.itemOrderMap[itemId] !== undefined) {
        const nextOrderMap = Object.assign({}, this.itemOrderMap)
        delete nextOrderMap[itemId]
        this.itemOrderMap = nextOrderMap
        this.saveItemOrder()
      }
      if (this.practiceQueue.length === 0) {
        this.clearPracticeFeedbackTimer()
        this.currentPracticeIndex = 0
        this.practiceAnswerDraft = ""
        this.practiceFlipped = false
        this.practiceFeedback = ""
        this.practiceOpen = false
        return
      }
      if (this.currentPracticeIndex >= this.practiceQueue.length && this.practiceQueue.length > 0) {
        this.currentPracticeIndex = this.practiceQueue.length - 1
      }
      this.fillPracticeDraft()
    },
    removeItemStates(itemIds) {
      itemIds.forEach(itemId => {
        this.removeItemState(itemId)
      })
    },
    resetCsvInput() {
      this.csvFile = null
      if (this.$refs.csvInputRef) {
        this.$refs.csvInputRef.value = ""
      }
    },
    truncateText(text, maxLength) {
      const value = text || ""
      return value.length > maxLength ? value.slice(0, maxLength) + "..." : value
    },
    shuffleItems(sourceItems) {
      const result = sourceItems.slice()
      for (let i = result.length - 1; i > 0; i -= 1) {
        const randomIndex = Math.floor(Math.random() * (i + 1))
        const temp = result[i]
        result[i] = result[randomIndex]
        result[randomIndex] = temp
      }
      return result
    }
  }
}
</script>

<style scoped>
.flashcard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-card,
.import-card,
.practice-card {
  border-radius: 8px;
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-main {
  flex: 1;
  min-width: 320px;
}

.toolbar-actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.book-description {
  color: #606266;
  line-height: 20px;
  padding-left: 4px;
}

.import-row,
.quadrant-row {
  margin-top: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.practice-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.practice-toolbar-title {
  font-weight: 600;
  color: #303133;
}

.practice-toolbar-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quadrant-panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-top: 4px solid #409eff;
  border-radius: 8px;
  padding: 16px;
  height: 520px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.quadrant-panel.is-drag-over {
  border-color: #c6e2ff;
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.12);
}

.quadrant-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.quadrant-head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.quadrant-head strong {
  color: #303133;
}

.quadrant-head span,
.quadrant-subtitle,
.tile-meta,
.tile-answer,
.empty-state,
.action-label {
  color: #909399;
}

.quadrant-subtitle {
  font-size: 12px;
  margin-bottom: 16px;
}

.quadrant-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
}

.flashcard-tile {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
  cursor: grab;
  opacity: 1;
  transform: translateY(0) scale(1);
  transition:
    transform 0.22s cubic-bezier(0.2, 0.8, 0.2, 1),
    opacity 0.16s ease,
    border-color 0.16s ease,
    background-color 0.16s ease,
    box-shadow 0.16s ease;
  will-change: transform;
}

.flashcard-tile.is-dragging {
  opacity: 0.9;
  transform: scale(1.015);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.18);
  cursor: grabbing;
  z-index: 3;
}

.flashcard-tile.is-drop-preview {
  border: 1px dashed #409eff;
  background: rgba(64, 158, 255, 0.08);
  box-shadow: inset 0 0 0 1px rgba(64, 158, 255, 0.08);
}

.flashcard-tile.is-drop-preview > * {
  opacity: 0.28;
}

.flashcard-drag-image {
  opacity: 0.9;
  pointer-events: none;
  transform: scale(1.015);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.2);
}

.flashcard-tile.selected {
  border-color: #f56c6c;
  background: #fff7f7;
}

.flashcard-drag-move {
  transition: transform 0.22s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.tile-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.tile-title {
  font-weight: 600;
  color: #303133;
  line-height: 20px;
}

.tile-title-wrap {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.tile-check {
  flex: 0 0 auto;
  margin-top: 1px;
}

.tile-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.tile-meta {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
}

.tile-answer {
  margin-top: 8px;
  font-size: 12px;
  line-height: 18px;
}

.empty-state {
  min-height: 100%;
  display: flex;
  align-items: center;
}

.practice-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.practice-shell-modern {
  padding: 4px 10px 10px;
}

.practice-stage {
  position: relative;
}

.practice-stage-meta {
  position: absolute;
  top: 18px;
  left: 24px;
  right: 24px;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  pointer-events: none;
}

.practice-progress {
  color: rgba(255, 255, 255, 0.78);
  font-size: 18px;
  font-weight: 500;
}

.practice-status {
  color: rgba(255, 255, 255, 0.72);
  font-size: 18px;
  font-weight: 600;
}

.practice-status-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}

.practice-status.success {
  color: #22c55e;
}

.practice-status.danger {
  color: #f87171;
}

.practice-guide {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.86);
  font-size: 18px;
  font-weight: 600;
}

.practice-card-face {
  position: relative;
  border-radius: 36px;
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 88px 56px 72px;
}

.practice-card-face-modern {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background:
    radial-gradient(circle at 12% 12%, rgba(59, 130, 246, 0.12), transparent 28%),
    radial-gradient(circle at 84% 16%, rgba(34, 197, 94, 0.1), transparent 24%),
    #2f2f2f;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.24);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.practice-card-face-modern.flipped {
  background:
    radial-gradient(circle at 16% 18%, rgba(191, 219, 254, 0.65), transparent 30%),
    radial-gradient(circle at 84% 20%, rgba(220, 252, 231, 0.72), transparent 26%),
    linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  box-shadow: 0 24px 60px rgba(37, 99, 235, 0.2);
}

.practice-card-face-modern.flipped .practice-face-question {
  color: #0f172a;
}

.practice-card-face-modern.flipped .practice-face-hint {
  color: rgba(15, 23, 42, 0.45);
}

.practice-card-face-modern.known {
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.28), 0 24px 60px rgba(34, 197, 94, 0.18);
}

.practice-card-face-modern.retry {
  box-shadow: 0 0 0 2px rgba(248, 113, 113, 0.28), 0 24px 60px rgba(248, 113, 113, 0.18);
}

.practice-face {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  text-align: center;
}

.practice-face-content {
  width: 100%;
  white-space: pre-wrap;
}

.practice-face-question {
  color: #ffffff;
  font-size: 28px;
  line-height: 1.65;
  font-weight: 500;
}

.practice-face-hint {
  color: rgba(255, 255, 255, 0.42);
  font-size: 18px;
}

.practice-feedback-mask {
  position: absolute;
  inset: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 36px;
  pointer-events: none;
  animation: practiceFeedbackFade 0.65s ease both;
}

.practice-feedback-mask.known {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1), rgba(34, 197, 94, 0.22));
}

.practice-feedback-mask.retry {
  background: linear-gradient(135deg, rgba(248, 113, 113, 0.12), rgba(248, 113, 113, 0.22));
}

.practice-feedback-mask span {
  font-size: 88px;
  font-weight: 700;
}

.practice-feedback-mask.known span {
  color: #22c55e;
}

.practice-feedback-mask.retry span {
  color: #f87171;
}

.practice-answer-compare {
  display: grid;
  gap: 18px;
  text-align: left;
}

.practice-answer-block {
  border-radius: 20px;
  padding: 20px 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

.practice-answer-block span {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
}

.practice-answer-block p {
  margin: 0;
  font-size: 18px;
  line-height: 1.7;
  color: #0f172a;
  white-space: pre-wrap;
}

.practice-answer-block.user-answer {
  border-left: 4px solid #60a5fa;
}

.practice-answer-block.standard-answer {
  border-left: 4px solid #22c55e;
}

.practice-answer-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 14px;
  align-items: flex-start;
}

::v-deep .practice-answer-input .el-textarea__inner {
  min-height: 110px !important;
  border-radius: 20px;
  padding: 18px 20px;
  color: #0f172a;
  background: #f8fafc;
}

.practice-answer-save {
  min-width: 108px;
  height: 46px;
  border-radius: 999px;
  align-self: center;
}

.practice-judge-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 34px;
}

.practice-secondary-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 20px;
  border: 1px solid #ebeef5;
  border-radius: 22px;
  background: #fafcff;
}

.practice-secondary-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.practice-card-meta {
  color: #909399;
  font-size: 12px;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.round-nav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border: 1.5px solid rgba(37, 99, 235, 0.72);
  border-radius: 50%;
  color: #2563eb;
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08);
  transition: all 0.2s ease;
}

.round-nav-btn span {
  font-size: 24px;
  line-height: 1;
}

.round-nav-btn:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.judge-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 92px;
  height: 56px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08);
  transition: all 0.2s ease;
  gap: 10px;
}

.judge-pill span {
  font-size: 30px;
  line-height: 1;
}

.judge-pill em {
  font-style: normal;
  font-size: 22px;
  font-weight: 500;
  line-height: 1;
}

.judge-pill:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.wrong-btn {
  color: #ef4444;
}

.right-btn {
  color: #16a34a;
}

@keyframes practiceFeedbackFade {
  0% {
    opacity: 0;
    transform: scale(0.96);
  }

  30% {
    opacity: 1;
    transform: scale(1.02);
  }

  100% {
    opacity: 0;
    transform: scale(1.04);
  }
}

@media (max-width: 768px) {
  .toolbar-row,
  .practice-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-actions {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .quadrant-panel {
    height: 360px;
  }

  .practice-card-face {
    min-height: 380px;
    padding: 72px 24px 54px;
    border-radius: 24px;
  }

  .practice-face-question {
    font-size: 22px;
  }

  .practice-answer-row {
    grid-template-columns: 1fr;
  }

  .practice-judge-row {
    gap: 14px;
  }

  .judge-pill {
    min-width: 72px;
  }

  .practice-stage-meta {
    left: 18px;
    right: 18px;
  }
}
</style>
