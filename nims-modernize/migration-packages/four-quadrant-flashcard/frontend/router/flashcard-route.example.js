// CN: flashcard page route example for migration
// EN: flashcard page route example for migration
export default [
  {
    path: '/Flashcard/FourQuadrantFlashcard',
    name: 'FourQuadrantFlashcard',
    component: () => import('@/views/Flashcard/FourQuadrantFlashcard.vue'),
    meta: {
      title: 'Four Quadrant Flashcard',
    },
  },
];
