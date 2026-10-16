<template>
  <div class="app-container flashcard-manage">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" size="small">
        <el-form-item label="Book"><el-select v-model="query.bookId" clearable filterable placeholder="All books" style="width: 180px" @change="filter"><el-option v-for="book in books" :key="book.bookId" :label="book.bookName" :value="book.bookId" /></el-select></el-form-item>
        <el-form-item label="Keyword"><el-input v-model.trim="query.keyword" clearable placeholder="Search front or answer" style="width: 220px" @keyup.enter.native="filter" /></el-form-item>
        <el-form-item label="Quadrant"><el-select v-model="query.quadrantType" clearable placeholder="All quadrants" style="width: 150px" @change="filter"><el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" /></el-select></el-form-item>
        <el-form-item label="Status"><el-select v-model="query.status" clearable placeholder="All status" style="width: 130px" @change="filter"><el-option label="Pending" value="pending" /><el-option label="Reviewing" value="reviewing" /><el-option label="Mastered" value="mastered" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="filter">Search</el-button><el-button icon="el-icon-refresh" @click="reset">Reset</el-button></el-form-item>
      </el-form>
    </el-card>
    <div class="stats"><div v-for="stat in stats" :key="stat.label" class="stat" :style="{ borderTopColor: stat.color }"><span>{{ stat.label }}</span><strong>{{ stat.value }}</strong></div></div>
    <el-card shadow="never">
      <div slot="header" class="table-header"><span>Flashcard Management</span><div><el-button type="primary" size="small" icon="el-icon-plus" @click="openEditor('add')" v-hasPermi="['study:flashcard:add']">Add</el-button><el-button type="success" size="small" icon="el-icon-rank" :disabled="!selected.length" @click="batchMoveOpen = true" v-hasPermi="['study:flashcard:edit']">Move</el-button><el-button type="danger" size="small" icon="el-icon-delete" :disabled="!selected.length" @click="batchDelete" v-hasPermi="['study:flashcard:remove']">Delete</el-button><el-button size="small" icon="el-icon-refresh" @click="loadItems">Refresh</el-button></div></div>
      <el-table v-loading="loading" :data="pageItems" border stripe @selection-change="selected = $event">
        <el-table-column type="selection" width="48" /><el-table-column label="Front content" min-width="260"><template slot-scope="scope"><el-button type="text" @click="showDetail(scope.row)">{{ short(scope.row.frontContent, 56) }}</el-button></template></el-table-column>
        <el-table-column label="Book" min-width="130"><template slot-scope="scope">{{ bookName(scope.row.bookId) }}</template></el-table-column><el-table-column label="Quadrant" width="160"><template slot-scope="scope"><el-tag :type="quadrantTag(scope.row.quadrantType)" size="small">{{ quadrantName(scope.row.quadrantType) }}</el-tag></template></el-table-column><el-table-column label="Status" width="100"><template slot-scope="scope"><el-tag :type="statusTag(scope.row)" size="small">{{ statusName(scope.row) }}</el-tag></template></el-table-column>
        <el-table-column label="Reviews" prop="reviewCount" width="80" align="center" /><el-table-column label="Score" width="70" align="center"><template slot-scope="scope">{{ scope.row.difficultyScore || '-' }}</template></el-table-column><el-table-column label="Last answer" min-width="180"><template slot-scope="scope">{{ short(scope.row.lastUserAnswer || 'None', 30) }}</template></el-table-column>
        <el-table-column label="Actions" width="180" fixed="right"><template slot-scope="scope"><el-button type="text" size="small" @click="showDetail(scope.row)">View</el-button><el-button type="text" size="small" @click="openEditor('edit', scope.row)" v-hasPermi="['study:flashcard:edit']">Edit</el-button><el-button type="text" size="small" @click="copy(scope.row)" v-hasPermi="['study:flashcard:add']">Copy</el-button><el-button type="text" size="small" class="danger" @click="remove(scope.row)" v-hasPermi="['study:flashcard:remove']">Delete</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="filtered.length" :total="filtered.length" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="noop" />
    </el-card>
    <el-drawer title="Flashcard detail" :visible.sync="detailOpen" size="440px" append-to-body><div v-if="detail" class="detail"><div class="detail-meta"><el-tag :type="quadrantTag(detail.quadrantType)" size="small">{{ quadrantName(detail.quadrantType) }}</el-tag><span>{{ bookName(detail.bookId) }}</span></div><h3>Front content</h3><p>{{ detail.frontContent }}</p><h3>Back answer</h3><p>{{ detail.backContent }}</p><h3>Review information</h3><p>Status: {{ statusName(detail) }} | Reviews: {{ detail.reviewCount || 0 }} | Score: {{ detail.difficultyScore || '-' }}</p><h3>Last input</h3><p>{{ detail.lastUserAnswer || 'None' }}</p></div></el-drawer>
    <el-dialog :title="editorMode === 'add' ? 'Add flashcard' : 'Edit flashcard'" :visible.sync="editorOpen" width="680px" append-to-body><el-form ref="form" :model="editor" :rules="rules" label-width="100px"><el-form-item label="Book" prop="bookId"><el-select v-model="editor.bookId" filterable style="width: 100%"><el-option v-for="book in books" :key="book.bookId" :label="book.bookName" :value="book.bookId" /></el-select></el-form-item><el-form-item label="Front content" prop="frontContent"><el-input v-model="editor.frontContent" type="textarea" :rows="5" /></el-form-item><el-form-item label="Back answer" prop="backContent"><el-input v-model="editor.backContent" type="textarea" :rows="5" /></el-form-item><el-form-item label="Quadrant" prop="quadrantType"><el-select v-model="editor.quadrantType" style="width: 100%"><el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" /></el-select></el-form-item></el-form><div slot="footer"><el-button type="primary" @click="submit">Submit</el-button><el-button @click="editorOpen = false">Cancel</el-button></div></el-dialog>
    <el-dialog title="Move selected cards" :visible.sync="batchMoveOpen" width="420px" append-to-body><el-select v-model="batchQuadrant" style="width: 100%"><el-option v-for="quadrant in quadrants" :key="quadrant.type" :label="quadrant.title" :value="quadrant.type" /></el-select><div slot="footer"><el-button type="primary" :disabled="!batchQuadrant" @click="batchMove">Submit</el-button><el-button @click="batchMoveOpen = false">Cancel</el-button></div></el-dialog>
  </div>
</template>
<script>
import { listBook, listItem, addItem, updateItem, delItem, moveItem } from '@/api/study/flashcard'
export default {
  name: 'StudyFlashcardManage',
  data() {
    return {
      loading: false, books: [], items: [], selected: [], detail: null, detailOpen: false, editorOpen: false, editorMode: 'add', editor: {}, batchMoveOpen: false, batchQuadrant: undefined,
      query: { bookId: undefined, keyword: '', quadrantType: undefined, status: undefined, pageNum: 1, pageSize: 10 },
      quadrants: [{ type: 1, title: 'Q1 Important / Difficult' }, { type: 2, title: 'Q2 Important / Familiar' }, { type: 3, title: 'Q3 Minor / Difficult' }, { type: 4, title: 'Q4 Minor / Familiar' }],
      rules: { bookId: [{ required: true, message: 'Book is required', trigger: 'change' }], frontContent: [{ required: true, message: 'Front content is required', trigger: 'blur' }], backContent: [{ required: true, message: 'Back answer is required', trigger: 'blur' }], quadrantType: [{ required: true, message: 'Quadrant is required', trigger: 'change' }] }
    }
  },
  computed: {
    filtered() { const keyword = (this.query.keyword || '').toLowerCase(); return this.items.filter(item => (!keyword || [item.frontContent, item.backContent, item.lastUserAnswer].some(value => (value || '').toLowerCase().indexOf(keyword) !== -1)) && (!this.query.quadrantType || item.quadrantType === this.query.quadrantType) && (!this.query.status || this.itemStatus(item) === this.query.status)) },
    pageItems() { const start = (this.query.pageNum - 1) * this.query.pageSize; return this.filtered.slice(start, start + this.query.pageSize) },
    stats() { const count = key => this.items.filter(item => key === 'total' || item.quadrantType === key || this.itemStatus(item) === key).length; return [{ label: 'Total', value: count('total'), color: '#409eff' }, { label: 'Pending', value: count('pending'), color: '#e6a23c' }, { label: 'Mastered', value: count('mastered'), color: '#67c23a' }, { label: 'Q1', value: count(1), color: '#f56c6c' }, { label: 'Q2', value: count(2), color: '#409eff' }, { label: 'Q3', value: count(3), color: '#e6a23c' }, { label: 'Q4', value: count(4), color: '#67c23a' }] }
  },
  created() { this.loadBooks() },
  methods: {
    loadBooks() { listBook().then(response => { this.books = response.data || []; this.loadItems() }) },
    loadItems() { this.loading = true; const requests = this.query.bookId ? [listItem({ bookId: this.query.bookId })] : this.books.map(book => listItem({ bookId: book.bookId })); Promise.all(requests).then(responses => { this.items = responses.reduce((all, response) => all.concat(response.data || []), []); this.query.pageNum = 1 }).finally(() => { this.loading = false }) },
    filter() { this.query.pageNum = 1 },
    reset() { Object.assign(this.query, { bookId: undefined, keyword: '', quadrantType: undefined, status: undefined, pageNum: 1 }); this.loadItems() },
    noop() {},
    bookName(id) { const book = this.books.find(item => item.bookId === id); return book ? book.bookName : '-' },
    quadrantName(type) { const quadrant = this.quadrants.find(item => item.type === type); return quadrant ? quadrant.title : '-' },
    quadrantTag(type) { return ({ 1: 'danger', 2: '', 3: 'warning', 4: 'success' })[type] },
    itemStatus(item) { return !item.reviewCount ? 'pending' : Number(item.difficultyScore) >= 4 ? 'mastered' : 'reviewing' },
    statusName(item) { return ({ pending: 'Pending', reviewing: 'Reviewing', mastered: 'Mastered' })[this.itemStatus(item)] },
    statusTag(item) { return ({ pending: 'warning', reviewing: '', mastered: 'success' })[this.itemStatus(item)] },
    short(text, length) { const value = text || ''; return value.length > length ? value.slice(0, length) + '...' : value },
    showDetail(item) { this.detail = item; this.detailOpen = true },
    openEditor(mode, item) { this.editorMode = mode; this.editor = mode === 'edit' ? Object.assign({}, item) : Object.assign({}, item || {}, { itemId: undefined, bookId: (item && item.bookId) || this.query.bookId || (this.books[0] && this.books[0].bookId), frontContent: (item && item.frontContent) || '', backContent: (item && item.backContent) || '', quadrantType: (item && item.quadrantType) || 1 }); this.editorOpen = true; this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate()) },
    submit() { this.$refs.form.validate(valid => { if (!valid) return; const request = this.editorMode === 'edit' ? updateItem(this.editor) : addItem(this.editor); request.then(() => { this.$modal.msgSuccess(this.editorMode === 'edit' ? 'Updated' : 'Added'); this.editorOpen = false; this.loadItems() }) }) },
    copy(item) { this.openEditor('add', item) },
    remove(item) { this.$modal.confirm('Delete this flashcard?').then(() => delItem(item.itemId)).then(() => { this.$modal.msgSuccess('Deleted'); this.loadItems() }).catch(() => {}) },
    batchDelete() { const ids = this.selected.map(item => item.itemId); this.$modal.confirm('Delete ' + ids.length + ' selected flashcards?').then(() => delItem(ids.join(','))).then(() => { this.$modal.msgSuccess('Deleted'); this.loadItems() }).catch(() => {}) },
    batchMove() { Promise.all(this.selected.map(item => moveItem({ itemId: item.itemId, quadrantType: this.batchQuadrant }))).then(() => { this.$modal.msgSuccess('Moved'); this.batchMoveOpen = false; this.loadItems() }) }
  }
}
</script>
<style lang="scss" scoped>
.flashcard-manage { background: #f5f7fa; min-height: calc(100vh - 84px); }.filter-card { margin-bottom: 16px; }.stats { display: grid; grid-template-columns: repeat(7, minmax(90px, 1fr)); gap: 12px; margin-bottom: 16px; }.stat { background: #fff; border-top: 3px solid; padding: 12px 16px; min-height: 72px; box-sizing: border-box; }.stat span { color: #909399; font-size: 13px; }.stat strong { display: block; color: #303133; font-size: 24px; line-height: 32px; }.table-header { display: flex; align-items: center; justify-content: space-between; }.danger { color: #f56c6c; }.detail { padding: 0 22px 24px; color: #606266; }.detail-meta { display: flex; justify-content: space-between; }.detail h3 { margin: 20px 0 8px; color: #303133; font-size: 14px; }.detail p { white-space: pre-wrap; word-break: break-word; line-height: 1.7; background: #f5f7fa; padding: 10px 12px; margin: 0; }@media (max-width: 900px) { .stats { grid-template-columns: repeat(4, minmax(90px, 1fr)); }.table-header { align-items: flex-start; flex-direction: column; gap: 10px; } }@media (max-width: 600px) { .stats { grid-template-columns: repeat(2, minmax(100px, 1fr)); } }
</style>
