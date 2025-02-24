package ui;

import game.WordLabyrinth;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameUI extends JFrame {
    private WordLabyrinth game;
    private JPanel gridPanel;
    private JLabel scoreLabel;
    private JLabel movesLabel;
    private JLabel wordsLabel;
    private JLabel currentWordLabel;
    private JButton[][] cellButtons;
    private JTextArea foundWordsArea;
    private String playerName;
    private HighScoreManager highScoreManager;
    private static final Color LAST_CLICKED_COLOR = new Color(255, 182, 193);  // Light pink
    private static final Color[] PATH_COLORS = {
        new Color(135, 206, 250),  // Light blue
        new Color(255, 218, 185),  // Peach
        new Color(216, 191, 216)   // Thistle
    };
    private static final Color VALIDATED_WORD_COLOR = new Color(144, 238, 144);  // Light green
    private Cell lastClickedCell;
    private int currentPathColorIndex = 0;
    private Map<List<Cell>, Integer> pathColors;  // Maps paths to their color indices

    public GameUI() {
        setTitle("Word Labyrinth");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        highScoreManager = new HighScoreManager();
        pathColors = new HashMap<>();
        getPlayerNameAndStart();
    }

    private void getPlayerNameAndStart() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:", "Player Name", JOptionPane.QUESTION_MESSAGE);
        if (playerName == null || playerName.trim().isEmpty()) {
            System.exit(0);
        }
        initializeGame();
    }

    private void initializeGame() {
        // Get grid size
        JTextField rowsField = new JTextField("8");
        JTextField colsField = new JTextField("8");
        Object[] message = {
            "Number of rows (5-15):", rowsField,
            "Number of columns (5-15):", colsField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Grid Size", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        // Validate and get grid size
        int rows, cols;
        try {
            rows = Integer.parseInt(rowsField.getText().trim());
            cols = Integer.parseInt(colsField.getText().trim());
            rows = Math.max(5, Math.min(15, rows));
            cols = Math.max(5, Math.min(15, cols));
        } catch (NumberFormatException e) {
            rows = cols = 8;
        }

        // Get difficulty level
        Object[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(this,
            "Select difficulty level:",
            "Word Labyrinth",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        model.Dictionary.DifficultyLevel level;
        switch (choice) {
            case 1:
                level = model.Dictionary.DifficultyLevel.MEDIUM;
                break;
            case 2:
                level = model.Dictionary.DifficultyLevel.HARD;
                break;
            default:
                level = model.Dictionary.DifficultyLevel.EASY;
        }

        game = new WordLabyrinth(level, rows, cols);
        createUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUI() {
        setLayout(new BorderLayout());

        // Create grid panel
        Grid grid = game.getGrid();
        gridPanel = new JPanel(new GridLayout(grid.getRows(), grid.getCols()));
        cellButtons = new JButton[grid.getRows()][grid.getCols()];

        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                Cell cell = grid.getCell(i, j);
                JButton button = new JButton(String.valueOf(cell.getLetter()));
                button.setPreferredSize(new Dimension(50, 50));
                button.setFont(new Font("Arial", Font.BOLD, 16));
                
                if (cell.isBlocked()) {
                    button.setBackground(Color.BLACK);
                    button.setEnabled(false);
                } else if (cell.isSpecial()) {
                    button.setBackground(Color.YELLOW);
                } else if (cell == grid.getStartCell()) {
                    button.setBackground(Color.GREEN);
                } else if (cell == grid.getDestinationCell()) {
                    button.setBackground(Color.RED);
                }

                final int row = i;
                final int col = j;
                button.addActionListener(e -> handleCellClick(row, col));
                
                cellButtons[i][j] = button;
                gridPanel.add(button);
            }
        }

        // Create info panel
        JPanel infoPanel = new JPanel(new GridLayout(5, 1));
        scoreLabel = new JLabel("Score: 0");
        movesLabel = new JLabel("Moves left: " + game.getMovesLeft());
        wordsLabel = new JLabel("Words found: 0/" + game.getRequiredWords());
        currentWordLabel = new JLabel("Current Word: ");
        
        foundWordsArea = new JTextArea(5, 20);
        foundWordsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(foundWordsArea);

        infoPanel.add(scoreLabel);
        infoPanel.add(movesLabel);
        infoPanel.add(wordsLabel);
        infoPanel.add(currentWordLabel);
        infoPanel.add(scrollPane);

        // Create control panel
        JPanel controlPanel = new JPanel();
        JButton submitButton = new JButton("Submit Word");
        submitButton.addActionListener(e -> handleSubmitWord());
        JButton resetButton = new JButton("Reset Path");
        resetButton.addActionListener(e -> resetPath());
        
        controlPanel.add(submitButton);
        controlPanel.add(resetButton);

        // Add panels to frame
        add(gridPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        updateUI();
    }

    private void showGameOver() {
        String scoreDetails = game.getFinalScoreDetails();
        String message = String.format("Game Over %s!\n\n%s", playerName, scoreDetails);

        // Check if it's a high score
        if (highScoreManager.isHighScore(game.getFinalScore())) {
            highScoreManager.addScore(playerName, game.getFinalScore());
            message += "\n\nNew High Score!";
        }

        // Show all high scores
        message += "\n\nHigh Scores:\n" + highScoreManager.getHighScoresText();

        int option = JOptionPane.showConfirmDialog(this,
            message + "\n\nWould you like to play again?",
            "Game Over",
            JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            new GameUI();
        } else {
            System.exit(0);
        }
    }

    private void handleSubmitWord() {
        if (game.submitWord()) {
            updateUI();
            
            // Check if all words are found
            if (game.isComplete()) {
                JOptionPane.showMessageDialog(this,
                    "Congratulations! You've found all the words in this labyrinth! \n" +
                    "You earn a 50-point bonus for your achievement!",
                    "Labyrinth Complete!",
                    JOptionPane.INFORMATION_MESSAGE);
                showGameOver();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid word or already found!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCellClick(int row, int col) {
        Cell cell = game.getGrid().getCell(row, col);
        
        // If it's the destination cell, end the game immediately
        if (cell == game.getGrid().getDestinationCell()) {
            showGameOver();
            return;
        }
        
        if (game.move(cell)) {
            lastClickedCell = cell;
            updateUI();
            
            if (game.hasWon() || game.hasLost()) {
                showGameOver();
            }
        }
    }

    private void resetPath() {
        // Save the current path's color before resetting
        if (!game.getCurrentPath().isEmpty()) {
            pathColors.put(new ArrayList<>(game.getCurrentPath()), currentPathColorIndex);
            game.resetPath();
        }
        // Cycle to next path color
        currentPathColorIndex = (currentPathColorIndex + 1) % PATH_COLORS.length;
        currentWordLabel.setText("Current Word: ");
        updateUI();
    }

    private void updateUI() {
        // Update labels
        updateScoreLabel();
        movesLabel.setText("Moves left: " + game.getMovesLeft());
        wordsLabel.setText("Words found: " + game.getFoundWords().size() + 
                          "/" + game.getRequiredWords());

        // Update current word
        StringBuilder currentWord = new StringBuilder("Current Word: ");
        for (Cell cell : game.getCurrentPath()) {
            currentWord.append(cell.getLetter());
        }
        currentWordLabel.setText(currentWord.toString());

        // Update found words area
        StringBuilder sb = new StringBuilder("Found Words:\n");
        for (String word : game.getFoundWords()) {
            sb.append(word).append("\n");
        }
        foundWordsArea.setText(sb.toString());

        // Update grid
        Grid grid = game.getGrid();
        java.util.List<Cell> currentPath = game.getCurrentPath();

        // First, reset all cell colors
        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                Cell cell = grid.getCell(i, j);
                JButton button = cellButtons[i][j];

                // Reset background for non-special cells
                if (!cell.isBlocked() && !cell.isSpecial() && 
                    cell != grid.getStartCell() && cell != grid.getDestinationCell()) {
                    button.setBackground(null);
                    button.setEnabled(true);
                }
            }
        }

        // Color previous paths
        for (List<Cell> path : game.getPreviousPaths()) {
            int colorIndex = pathColors.getOrDefault(path, currentPathColorIndex);
            for (Cell cell : path) {
                JButton button = cellButtons[cell.getRow()][cell.getCol()];
                if (cell.isUsed()) {
                    // If the cell is part of a validated word, use green
                    button.setBackground(VALIDATED_WORD_COLOR);
                } else {
                    // Otherwise, use the path's color
                    button.setBackground(PATH_COLORS[colorIndex]);
                }
            }
        }

        // Color current path
        for (Cell cell : currentPath) {
            JButton button = cellButtons[cell.getRow()][cell.getCol()];
            button.setBackground(PATH_COLORS[currentPathColorIndex]);
        }

        // Color special cells and last clicked cell
        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                Cell cell = grid.getCell(i, j);
                JButton button = cellButtons[i][j];

                if (cell == lastClickedCell) {
                    button.setBackground(LAST_CLICKED_COLOR);
                } else if (cell == grid.getStartCell()) {
                    button.setBackground(Color.GREEN);
                } else if (cell == grid.getDestinationCell()) {
                    button.setBackground(Color.RED);
                } else if (cell.isBlocked()) {
                    button.setBackground(Color.BLACK);
                    button.setEnabled(false);
                } else if (cell.isSpecial()) {
                    button.setBackground(Color.YELLOW);
                }
            }
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.format("Score: %d | Words Found: %d/%d | Moves Left: %d",
            game.getCurrentScore(), game.getFoundWords().size(), game.getRequiredWords(), game.getMovesLeft()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameUI gameUI = new GameUI();
        });
    }
}
