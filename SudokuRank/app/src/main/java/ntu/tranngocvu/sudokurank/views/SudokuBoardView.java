package ntu.tranngocvu.sudokurank.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SudokuBoardView extends View {

    private final Paint thinPaint = new Paint();
    private final Paint thickPaint = new Paint();
    private final Paint fixedPaint = new Paint();
    private final Paint selectedPaint = new Paint();
    private final Paint wrongPaint = new Paint();

    private final Paint normalTextPaint = new Paint();
    private final Paint fixedTextPaint = new Paint();
    private final Paint notePaint = new Paint();

    private final char[][] puzzle = new char[9][9];
    private final char[][] board = new char[9][9];
    private final String[][] notes = new String[9][9];

    private int selectedRow = -1;
    private int selectedCol = -1;

    private int wrongRow = -1;
    private int wrongCol = -1;

    private OnCellClickListener listener;

    public interface OnCellClickListener {
        void onCellClick(int row, int col);
    }

    public SudokuBoardView(Context context) {
        super(context);
        init();
    }

    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SudokuBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        thinPaint.setColor(Color.parseColor("#AAB4C3"));
        thinPaint.setStrokeWidth(2f);
        thinPaint.setAntiAlias(true);

        thickPaint.setColor(Color.parseColor("#1F2937"));
        thickPaint.setStrokeWidth(6f);
        thickPaint.setAntiAlias(true);

        fixedPaint.setColor(Color.parseColor("#EEF2F7"));
        selectedPaint.setColor(Color.parseColor("#BBDEFB"));
        wrongPaint.setColor(Color.parseColor("#FFCDD2"));

        normalTextPaint.setColor(Color.parseColor("#1976D2"));
        normalTextPaint.setTextAlign(Paint.Align.CENTER);
        normalTextPaint.setTextSize(40f);
        normalTextPaint.setAntiAlias(true);

        fixedTextPaint.setColor(Color.BLACK);
        fixedTextPaint.setTextAlign(Paint.Align.CENTER);
        fixedTextPaint.setTextSize(40f);
        fixedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        fixedTextPaint.setAntiAlias(true);

        notePaint.setColor(Color.GRAY);
        notePaint.setTextAlign(Paint.Align.CENTER);
        notePaint.setTextSize(17f);
        notePaint.setAntiAlias(true);
    }

    public void setBoard(String puzzleStr, String boardStr) {
        if (puzzleStr == null || boardStr == null) return;
        if (puzzleStr.length() != 81 || boardStr.length() != 81) return;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                puzzle[r][c] = puzzleStr.charAt(r * 9 + c);
                board[r][c] = boardStr.charAt(r * 9 + c);
                notes[r][c] = "";
            }
        }

        selectedRow = -1;
        selectedCol = -1;
        wrongRow = -1;
        wrongCol = -1;

        invalidate();
    }

    public boolean isFixedCell(int row, int col) {
        if (!isValidCell(row, col)) return true;
        return puzzle[row][col] != '0';
    }

    public void selectCell(int row, int col) {
        if (!isValidCell(row, col)) return;

        selectedRow = row;
        selectedCol = col;

        invalidate();
    }

    public void setNumber(int row, int col, char value) {
        if (!isValidCell(row, col)) return;
        if (isFixedCell(row, col)) return;

        board[row][col] = value;
        notes[row][col] = "";

        invalidate();
    }

    public void clearCell(int row, int col) {
        if (!isValidCell(row, col)) return;
        if (isFixedCell(row, col)) return;

        board[row][col] = '0';
        notes[row][col] = "";

        invalidate();
    }

    public void addNote(int row, int col, int num) {
        if (!isValidCell(row, col)) return;
        if (isFixedCell(row, col)) return;
        if (board[row][col] != '0') return;

        String old = notes[row][col];
        if (old == null) old = "";

        String n = String.valueOf(num);

        if (old.contains(n)) {
            old = old.replace(n, "");
        } else {
            old += n;
        }

        notes[row][col] = old;

        invalidate();
    }

    public void markWrongCell(int row, int col) {
        if (!isValidCell(row, col)) return;

        wrongRow = row;
        wrongCol = col;

        invalidate();
    }

    public void clearWrongCell() {
        wrongRow = -1;
        wrongCol = -1;

        invalidate();
    }

    public String getCurrentBoardString() {
        StringBuilder sb = new StringBuilder();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                char value = board[r][c];

                if (value >= '1' && value <= '9') {
                    sb.append(value);
                } else {
                    sb.append('0');
                }
            }
        }

        return sb.toString();
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.listener = listener;
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < 9 && col >= 0 && col < 9;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float cell = size / 9f;

        drawBackgrounds(canvas, cell);
        drawNumbers(canvas, cell);
        drawGrid(canvas, size, cell);
    }

    private void drawBackgrounds(Canvas canvas, float cell) {
        canvas.drawColor(Color.WHITE);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                float left = c * cell;
                float top = r * cell;
                float right = left + cell;
                float bottom = top + cell;

                if (puzzle[r][c] != '0') {
                    canvas.drawRect(left, top, right, bottom, fixedPaint);
                }

                if (r == selectedRow && c == selectedCol) {
                    canvas.drawRect(left, top, right, bottom, selectedPaint);
                }

                if (r == wrongRow && c == wrongCol) {
                    canvas.drawRect(left, top, right, bottom, wrongPaint);
                }
            }
        }
    }

    private void drawNumbers(Canvas canvas, float cell) {
        Paint.FontMetrics normalFm = normalTextPaint.getFontMetrics();
        float normalOffset = (normalFm.ascent + normalFm.descent) / 2;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                float x = c * cell + cell / 2f;
                float y = r * cell + cell / 2f - normalOffset;

                if (board[r][c] != '0') {
                    Paint paint = puzzle[r][c] != '0' ? fixedTextPaint : normalTextPaint;
                    canvas.drawText(String.valueOf(board[r][c]), x, y, paint);
                } else if (notes[r][c] != null && !notes[r][c].isEmpty()) {
                    drawNotes(canvas, notes[r][c], r, c, cell);
                }
            }
        }
    }

    private void drawNotes(Canvas canvas, String note, int row, int col, float cell) {
        for (int i = 0; i < note.length(); i++) {
            char ch = note.charAt(i);

            if (ch < '1' || ch > '9') continue;

            int numberIndex = ch - '1';
            int noteRow = numberIndex / 3;
            int noteCol = numberIndex % 3;

            float x = col * cell + (noteCol + 0.5f) * cell / 3f;
            float y = row * cell + (noteRow + 0.68f) * cell / 3f;

            canvas.drawText(String.valueOf(ch), x, y, notePaint);
        }
    }

    private void drawGrid(Canvas canvas, float size, float cell) {
        for (int i = 0; i <= 9; i++) {
            Paint paint = i % 3 == 0 ? thickPaint : thinPaint;
            float pos = i * cell;

            canvas.drawLine(pos, 0, pos, size, paint);
            canvas.drawLine(0, pos, size, pos, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        float size = Math.min(getWidth(), getHeight());
        float cell = size / 9f;

        int col = (int) (event.getX() / cell);
        int row = (int) (event.getY() / cell);

        if (isValidCell(row, col)) {
            selectedRow = row;
            selectedCol = col;

            if (listener != null) {
                listener.onCellClick(row, col);
            }

            invalidate();
        }

        return true;
    }
}