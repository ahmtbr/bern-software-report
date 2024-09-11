import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class SudokuGame extends JFrame {
    private static final int SIZE = 9;
    private JButton[][] cells;
    private SudokuBoard board;
    private SudokuController controller;
    private JButton checkSolutionButton;
    private JButton newGameButton;
    private JButton solvePuzzleButton;

    public SudokuGame() {
        this.board = new SudokuBoard();
        this.cells = new JButton[SIZE][SIZE];
        this.controller = new SudokuController(board, this);
        initGUI();
    }

    private void initGUI() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JButton button = new JButton();
                button.setFont(new Font("Arial", Font.BOLD, 20));
                button.setBackground(Color.WHITE);
                int currentRow = row;
                int currentCol = col;

                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!board.isFixedCell(currentRow, currentCol)) {
                            String input = JOptionPane.showInputDialog("Enter a number (1-9) or 0 to clear:");
                            try {
                                if (input == null || input.equals("")) {
                                    controller.processMove(currentRow, currentCol, 0);  // Empty the cell
                                } else {
                                    int num = Integer.parseInt(input);
                                    if (num >= 0 && num <= 9) {
                                        if (controller.isValidMove(currentRow, currentCol, num)) {
                                            controller.processMove(currentRow, currentCol, num);
                                            updateBoard();
                                        } else {
                                            JOptionPane.showMessageDialog(null, "Invalid move! The number conflicts with existing row, column, or 3x3 box.");
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Invalid input! Enter a number between 1 and 9, or 0 to clear.");
                                    }
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Invalid input! Enter a valid number.");
                            }
                        }
                    }
                });

                cells[row][col] = button;
                gridPanel.add(button);
            }
        }

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        checkSolutionButton = new JButton("Check Solution");
        checkSolutionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.isBoardComplete()) {
                    if (controller.checkSolution()) {
                        JOptionPane.showMessageDialog(null, "Congratulations! You've solved the puzzle!");
                    } else {
                        JOptionPane.showMessageDialog(null, "The solution is incorrect. Keep trying!");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "The board is not fully completed. Please complete it first.");
                }
            }
        });

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.generateNewPuzzle();
                updateBoard();
            }
        });

        solvePuzzleButton = new JButton("Solve Puzzle");
        solvePuzzleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.solvePuzzle();
                updateBoard();
            }
        });

        controlPanel.add(checkSolutionButton);
        controlPanel.add(newGameButton);
        controlPanel.add(solvePuzzleButton);

        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int number = board.getNumber(row, col);
                if (!board.isFixedCell(row, col)) {
                    if (number == 0) {
                        cells[row][col].setText("");
                        cells[row][col].setEnabled(true);
                        cells[row][col].setBackground(Color.WHITE);
                    } else {
                        cells[row][col].setText(String.valueOf(number));
                        cells[row][col].setEnabled(true);
                        cells[row][col].setBackground(Color.WHITE);
                    }
                } else {
                    cells[row][col].setText(String.valueOf(number));
                    cells[row][col].setEnabled(false);
                    cells[row][col].setBackground(Color.LIGHT_GRAY);
                }
            }
        }
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuGame::new);
    }

    // Sudoku Board class (Model)
    public static class SudokuBoard {
        private int[][] grid;
        private boolean[][] fixedCells;

        public SudokuBoard() {
            grid = new int[SIZE][SIZE];
            fixedCells = new boolean[SIZE][SIZE];
            generateBoard();
        }

        public void generateBoard() {
            // Clear the existing grid and fixed cells
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    grid[row][col] = 0;
                    fixedCells[row][col] = false;
                }
            }

            fillBoard();
            removeNumbers(40); // Adjust the number of blanks for difficulty
        }

        private boolean fillBoard() {
            int[] baseNumbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
            shuffleArray(baseNumbers); // Randomize numbers for base row
            
            // Fill the diagonal 3x3 blocks first to guarantee valid sections
            for (int i = 0; i < SIZE; i += 3) {
                fillDiagonalBlock(i, i, baseNumbers);
                shuffleArray(baseNumbers); // Shuffle again for variety in each block
            }

            return new SudokuSolver().solve(grid);
        }

        // Fill the diagonal 3x3 blocks
        private void fillDiagonalBlock(int rowStart, int colStart, int[] baseNumbers) {
            int idx = 0;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    grid[rowStart + row][colStart + col] = baseNumbers[idx++];
                }
            }
        }

        private void shuffleArray(int[] array) {
            Random rand = new Random();
            for (int i = array.length - 1; i > 0; i--) {
                int index = rand.nextInt(i + 1);
                int temp = array[index];
                array[index] = array[i];
                array[i] = temp;
            }
        }

        private void removeNumbers(int count) {
            Random rand = new Random();
            while (count > 0) {
                int row = rand.nextInt(SIZE);
                int col = rand.nextInt(SIZE);

                if (grid[row][col] != 0) {
                    grid[row][col] = 0;
                    fixedCells[row][col] = false;
                    count--;
                }
            }

            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (grid[i][j] != 0) {
                        fixedCells[i][j] = true;
                    }
                }
            }
        }

        public boolean isValidMove(int row, int col, int num) {
            return SudokuValidator.isValidMove(grid, row, col, num);
        }

        public boolean isSolved() {
            return SudokuValidator.isBoardValid(grid);
        }

        public boolean isComplete() {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (grid[row][col] == 0) {
                        return false; // There's still an empty cell
                    }
                }
            }
            return true; // The board is fully filled
        }

        public void setNumber(int row, int col, int num) {
            if (!fixedCells[row][col]) {
                grid[row][col] = num;
            }
        }

        public int getNumber(int row, int col) {
            return grid[row][col];
        }

        public boolean isFixedCell(int row, int col) {
            return fixedCells[row][col];
        }

        public int[][] getBoard() {
            return grid;
        }
    }

    // Sudoku Solver class
    public static class SudokuSolver {
        public boolean solve(int[][] grid) {
            return solveSudoku(grid, 0, 0);
        }

        private boolean solveSudoku(int[][] grid, int row, int col) {
            if (row == SIZE) {
                return true;
            }

            if (col == SIZE) {
                return solveSudoku(grid, row + 1, 0);
            }

            if (grid[row][col] != 0) {
                return solveSudoku(grid, row, col + 1);
            }

            for (int num = 1; num <= 9; num++) {
                if (SudokuValidator.isValidMove(grid, row, col, num)) {
                    grid[row][col] = num;
                    if (solveSudoku(grid, row, col + 1)) {
                        return true;
                    }
                    grid[row][col] = 0;
                }
            }
            return false;
        }
    }

    // Sudoku Validator class
    public static class SudokuValidator {
        public static boolean isValidMove(int[][] grid, int row, int col, int num) {
            return isValidRow(grid, row, num) && isValidCol(grid, col, num) && isValidBox(grid, row, col, num);
        }

        private static boolean isValidRow(int[][] grid, int row, int num) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == num) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isValidCol(int[][] grid, int col, int num) {
            for (int row = 0; row < SIZE; row++) {
                if (grid[row][col] == num) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isValidBox(int[][] grid, int row, int col, int num) {
            int startRow = row - row % 3;
            int startCol = col - col % 3;

            for (int i = startRow; i < startRow + 3; i++) {
                for (int j = startCol; j < startCol + 3; j++) {
                    if (grid[i][j] == num) {
                        return false;
                    }
                }
            }
            return true;
        }

        public static boolean isBoardValid(int[][] grid) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    int num = grid[row][col];
                    if (num != 0) {
                        grid[row][col] = 0;
                        if (!isValidMove(grid, row, col, num)) {
                            return false;
                        }
                        grid[row][col] = num;
                    }
                }
            }
            return true;
        }
    }

    // Sudoku Controller class
    public static class SudokuController {
        private SudokuBoard board;
        private SudokuGame gui;
        private SudokuSolver solver;

        public SudokuController(SudokuBoard board, SudokuGame gui) {
            this.board = board;
            this.gui = gui;
            this.solver = new SudokuSolver();
        }

        public void processMove(int row, int col, int num) {
            if (!board.isFixedCell(row, col)) {
                board.setNumber(row, col, num);  // Allow clearing the cell (num == 0)
                gui.updateBoard();
            }
        }

        public boolean isValidMove(int row, int col, int num) {
            return board.isValidMove(row, col, num);
        }

        public boolean isBoardComplete() {
            return board.isComplete();
        }

        public boolean checkSolution() {
            if (board.isSolved()) {
                return true;
            } else {
                gui.showMessage("The solution is incorrect.");
                return false;
            }
        }

        public void generateNewPuzzle() {
            board.generateBoard();
            gui.updateBoard();
        }

        public void solvePuzzle() {
            if (solver.solve(board.getBoard())) {
                gui.updateBoard();
                gui.showMessage("The puzzle has been solved!");
            } else {
                gui.showMessage("The puzzle could not be solved.");
            }
        }
    }
}
