package game;

import model.*;
import java.util.*;

/**
 * Main game class that manages the game state and logic.
 */
public class WordLabyrinth {
    private Grid grid;
    private model.Dictionary dictionary;
    private int wordScore; // Score from found words only
    private int specialCellBonus; // Bonus from special cells
    private int movesLeftBonus; // Bonus from moves left
    private int completionBonus; // Bonus for completing the game
    private List<String> foundWords;
    private List<Cell> currentPath;
    private List<List<Cell>> previousPaths;
    private int movesLeft;
    private int requiredWords;

    public WordLabyrinth(model.Dictionary.DifficultyLevel level, int rows, int cols) {
        dictionary = new model.Dictionary();
        dictionary.setDifficultyLevel(level);
        foundWords = new ArrayList<>();
        currentPath = new ArrayList<>();
        previousPaths = new ArrayList<>();
        wordScore = 0;
        specialCellBonus = 0;
        movesLeftBonus = 0;
        completionBonus = 0;
        initializeGame(level, rows, cols);
    }

    private void initializeGame(model.Dictionary.DifficultyLevel level, int rows, int cols) {
        // Set game parameters based on difficulty
        switch (level) {
            case EASY:
                movesLeft = rows * cols;
                requiredWords = Math.max(5, rows * cols / 10);
                break;
            case MEDIUM:
                movesLeft = (rows * cols) * 3/4;
                requiredWords = Math.max(8, rows * cols / 8);
                break;
            case HARD:
                movesLeft = (rows * cols) / 2;
                requiredWords = Math.max(12, rows * cols / 6);
                break;
            default:
                movesLeft = rows * cols;
                requiredWords = Math.max(5, rows * cols / 10);
        }

        // Create grid with required number of words
        grid = new Grid(rows, cols, requiredWords);
        grid.initializeGrid(dictionary.getWords());
    }

    private void setRandomStartAndDestination() {
        Random random = new Random();
        int rows = grid.getRows();
        int cols = grid.getCols();

        // Set start cell
        while (grid.getStartCell() == null) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            Cell cell = grid.getCell(row, col);
            if (!cell.isBlocked()) {
                grid.setStartCell(cell);
            }
        }

        // Set destination cell
        while (grid.getDestinationCell() == null) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            Cell cell = grid.getCell(row, col);
            if (!cell.isBlocked() && cell != grid.getStartCell()) {
                grid.setDestinationCell(cell);
            }
        }
    }

    public boolean move(Cell cell) {
        if (movesLeft <= 0) return false;
        
        // Check if move is valid
        if (currentPath.isEmpty()) {
            // First move can be anywhere except blocked cells and destination
            if (cell.isBlocked() || cell == grid.getDestinationCell()) return false;
        } else {
            Cell lastCell = currentPath.get(currentPath.size() - 1);
            if (!grid.getNeighbors(lastCell).contains(cell)) return false;
            if (cell.isBlocked()) return false;
        }

        // Add cell to path if not already in path
        if (!currentPath.contains(cell)) {
            currentPath.add(cell);
            movesLeft--;
            return true;
        }
        
        return false;
    }

    public boolean submitWord() {
        if (currentPath.isEmpty()) return false;

        StringBuilder word = new StringBuilder();
        for (Cell cell : currentPath) {
            word.append(cell.getLetter());
        }

        String wordStr = word.toString().toLowerCase();
        if (dictionary.isValidWord(wordStr) && !foundWords.contains(wordStr)) {
            // Calculate word score based on difficulty
            int currentWordScore = dictionary.getWordScore(wordStr);
            wordScore += currentWordScore;
            
            // Add special cell bonus
            int cellBonus = 0;
            for (Cell cell : currentPath) {
                if (cell.isSpecial()) {
                    cellBonus += 25;  // Bonus for each special cell used
                }
            }
            specialCellBonus += cellBonus;

            foundWords.add(wordStr);

            // Mark cells as used
            for (Cell cell : currentPath) {
                cell.setUsed(true);
            }

            // Add current path to previous paths before clearing
            previousPaths.add(new ArrayList<>(currentPath));
            currentPath.clear();

            return true;
        }

        return false;
    }

    // Get current score (only words found)
    public int getCurrentScore() {
        return wordScore;
    }

    // Get final detailed score
    public String getFinalScoreDetails() {
        StringBuilder details = new StringBuilder();
        details.append(String.format("Score for words = %d\n", wordScore));
        details.append(String.format("Special cell bonus = %d\n", specialCellBonus));
        details.append(String.format("Moves left bonus = %d\n", movesLeft));
        
        int completionBonus = isComplete() ? 50 : 0;
        details.append(String.format("Completion bonus = %d\n", completionBonus));
        
        int totalScore = wordScore + specialCellBonus + movesLeft + completionBonus;
        details.append(String.format("\nTotal Score = %d", totalScore));
        
        return details.toString();
    }

    // Get final total score
    public int getFinalScore() {
        int finalScore = wordScore + specialCellBonus;
        
        // Add moves left bonus only if at least one word was found
        if (!foundWords.isEmpty()) {
            finalScore += movesLeft;
        }
        
        // Add completion bonus if all words found
        if (isComplete()) {
            finalScore += 50;
        }
        
        return finalScore;
    }

    public void resetPath() {
        if (!currentPath.isEmpty()) {
            previousPaths.add(new ArrayList<>(currentPath));
        }
        currentPath.clear();
    }

    public boolean hasWon() {
        return foundWords.size() >= requiredWords;
    }

    public boolean hasLost() {
        return movesLeft <= 0;
    }

    public boolean isComplete() {
        return foundWords.size() >= requiredWords;
    }

    // Getters
    public Grid getGrid() { return grid; }
    public List<String> getFoundWords() { return new ArrayList<>(foundWords); }
    public int getMovesLeft() { return movesLeft; }
    public int getRequiredWords() { return requiredWords; }
    public List<Cell> getCurrentPath() { return new ArrayList<>(currentPath); }
    public List<List<Cell>> getPreviousPaths() { return previousPaths; }
}
