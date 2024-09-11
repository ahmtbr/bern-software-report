import java.awt.*;
import javax.swing.*;

public class SudokuGame extends JFrame {
    private int[][] board;
    private JTextField[][] cells;
    private final int SIZE = 9;
    private Timer timer;
    private int secondsElapsed = 0;

    public SudokuGame() {
        // Initialize the game board with a sample puzzle
        board = new int[SIZE][SIZE];
        cells = new JTextField[SIZE][SIZE];
        setTitle("Sudoku Game");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a panel for the Sudoku grid
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));

        // Initialize cells (JTextFields) to represent the grid
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                gridPanel.add(cells[row][col]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        // Display the initial puzzle and create control buttons
        displayBoard();
        startTimer();
        createControlButtons();

        setVisible(true);
    }

    // Sample puzzle for demonstration purposes
    private int[][] samplePuzzle = {
        {5, 3, 0, 0, 7, 0, 0, 0, 0},
        {6, 0, 0, 1, 9, 5, 0, 0, 0},
        {0, 9, 8, 0, 0, 0, 0, 6, 0},
        {8, 0, 0, 0, 6, 0, 0, 0, 3},
        {4, 0, 0, 8, 0, 3, 0, 0, 1},
        {7, 0, 0, 0, 2, 0, 0, 0, 6},
        {0, 6, 0, 0, 0, 0, 2, 8, 0},
        {0, 0, 0, 4, 1, 9, 0, 0, 5},
        {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };

    // Display the initial Sudoku puzzle in the GUI
    private void displayBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int value = samplePuzzle[row][col];
                board[row][col] = value;
                if (value != 0) {
                    cells[row][col].setText(String.valueOf(value));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                }
            }
        }
        addInputListeners();
    }

    // Validate if a move is valid according to Sudoku rules
    private boolean isValidMove(int row, int col, int number) {
        // Check if number is in the row
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == number) {
                return false;
            }
        }

        // Check if number is in the column
        for (int i = 0; i < SIZE; i++) {
            if (board[i][col] == number) {
                return false;
            }
        }

        // Check if number is in the 3x3 grid
        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        for (int i = boxRowStart; i < boxRowStart + 3; i++) {
            for (int j = boxColStart; j < boxColStart + 3; j++) {
                if (board[i][j] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    // Add input listeners to cells to capture user input and validate moves
    private void addInputListeners() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int finalRow = row;
                int finalCol = col;
                if (cells[row][col].isEditable()) {
                    cells[row][col].addActionListener(e -> {
                        String input = cells[finalRow][finalCol].getText();
                        try {
                            int value = Integer.parseInt(input);
                            if (isValidMove(finalRow, finalCol, value)) {
                                board[finalRow][finalCol] = value;
                                cells[finalRow][finalCol].setBackground(Color.WHITE);
                                if (isSolved()) {
                                    stopTimer();
                                    JOptionPane.showMessageDialog(this, "Congratulations, you solved the puzzle!");
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "Invalid move! Try again.");
                                cells[finalRow][finalCol].setBackground(Color.RED);
                                cells[finalRow][finalCol].setText("");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                            cells[finalRow][finalCol].setText("");
                            cells[finalRow][finalCol].setBackground(Color.RED);
                        }
                    });
                }
            }
        }
    }

    // Check if the board is completely solved
    private boolean isSolved() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Create buttons for New Puzzle, Solve, and Restart
    private void createControlButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // New Puzzle button
        JButton newPuzzleButton = new JButton("New Puzzle");
        newPuzzleButton.addActionListener(e -> {
            displayBoard();
        });
        buttonPanel.add(newPuzzleButton);

        // Solve button
        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(e -> {
            solvePuzzle();
            displayBoard();
        });
        buttonPanel.add(solveButton);

        // Restart button
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> {
            displayBoard();
        });
        buttonPanel.add(restartButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Solve the puzzle using a simple backtracking algorithm
    private boolean solvePuzzle() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {  // Find an empty cell
                    for (int number = 1; number <= SIZE; number++) {
                        if (isValidMove(row, col, number)) {
                            board[row][col] = number;
                            if (solvePuzzle()) {
                                return true;
                            }
                            board[row][col] = 0;  // Backtrack
                        }
                    }
                    return false;  // No valid number found, backtrack
                }
            }
        }
        return true;  // Puzzle solved
    }

    // Start a timer to track how long the player takes to solve the puzzle
    private void startTimer() {
        timer = new Timer(1000, e -> {
            secondsElapsed++;
            setTitle("Sudoku Game - Time: " + secondsElapsed + " seconds");
        });
        timer.start();
    }

    // Stop the timer when the puzzle is solved
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public static void main(String[] args) {
        new SudokuGame();
    }
}
