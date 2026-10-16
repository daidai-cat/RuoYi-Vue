import request from '@/utils/request'

// 查询当前用户卡组列表
// Query current user flashcard book list
export function listBook(query) {
  return request({
    url: '/study/flashcard/book/list',
    method: 'get',
    params: query
  })
}

// 查询卡组详情
// Query flashcard book detail
export function getBook(bookId) {
  return request({
    url: '/study/flashcard/book/' + bookId,
    method: 'get'
  })
}

// 新增卡组
// Insert flashcard book
export function addBook(data) {
  return request({
    url: '/study/flashcard/book',
    method: 'post',
    data: data
  })
}

// 修改卡组
// Update flashcard book
export function updateBook(data) {
  return request({
    url: '/study/flashcard/book',
    method: 'put',
    data: data
  })
}

// 删除卡组
// Delete flashcard book
export function delBook(bookId) {
  return request({
    url: '/study/flashcard/book/' + bookId,
    method: 'delete'
  })
}

// 查询当前用户卡片列表
// Query current user flashcard item list
export function listItem(query) {
  return request({
    url: '/study/flashcard/item/list',
    method: 'get',
    params: query
  })
}

// 查询卡片详情
// Query flashcard item detail
export function getItem(itemId) {
  return request({
    url: '/study/flashcard/item/' + itemId,
    method: 'get'
  })
}

// 新增卡片
// Insert flashcard item
export function addItem(data) {
  return request({
    url: '/study/flashcard/item',
    method: 'post',
    data: data
  })
}

// 修改卡片
// Update flashcard item
export function updateItem(data) {
  return request({
    url: '/study/flashcard/item',
    method: 'put',
    data: data
  })
}

// 删除卡片
// Delete flashcard item
export function delItem(itemId) {
  return request({
    url: '/study/flashcard/item/' + itemId,
    method: 'delete'
  })
}

// 移动卡片象限
// Move flashcard item quadrant
export function moveItem(data) {
  return request({
    url: '/study/flashcard/item/move',
    method: 'put',
    data: data
  })
}

// 保存卡片评分
// Save flashcard item score
export function scoreItem(data) {
  return request({
    url: '/study/flashcard/item/score',
    method: 'put',
    data: data
  })
}

// 保存最近输入答案
// Save latest typed answer
export function saveItemAnswer(data) {
  return request({
    url: '/study/flashcard/item/answer',
    method: 'put',
    data: data
  })
}

// JSON 批量导入
// JSON batch import
export function importJsonItems(data) {
  return request({
    url: '/study/flashcard/item/import/json',
    method: 'post',
    data: data
  })
}

// CSV 导入
// CSV import
export function importCsvItems(bookId, quadrantType, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/study/flashcard/item/import/csv',
    method: 'post',
    params: {
      bookId: bookId,
      quadrantType: quadrantType
    },
    headers: {
      'Content-Type': 'multipart/form-data',
      repeatSubmit: false
    },
    data: formData
  })
}
