package game;

import model.*;
import java.util.*;

/**
 * Main game class that manages the game state and logic.
 */
public class WordLabyrinth {
    private Grid grid;
    private model.Dictionary dictionary;
    private int score;
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
        initializeGame(level, rows, cols);
    }

    private void initializeGame(model.Dictionary.DifficultyLevel level, int rows, int cols) {
        // Set game parameters based on difficulty
        switch (level) {
            case EASY:
                movesLeft = rows * cols ;  // More moves for larger grids
                requiredWords = Math.max(5, rows * cols / 10);  // At least 5 words, or 10% of grid size
                break;
            case MEDIUM:
                movesLeft = rows * cols * 3;
                requiredWords = Math.max(8, rows * cols / 8);
                break;
            case HARD:
                movesLeft = rows * cols * 4;
                requiredWords = Math.max(12, rows * cols / 6);
                break;
            default:
                movesLeft = rows * cols ;
                requiredWords = Math.max(5, rows * cols / 10);
        }

        grid = new Grid(rows, cols, dictionary.getCurrentDictionary());
        
        // Add blocked and special cells based on difficulty and grid size
        int blockedCells = (int)(rows * cols * 0.1);  // 10% of cells are blocked
        int specialCells = (int)(rows * cols * 0.05);  // 5% of cells are special
        grid.addBlockedCells(blockedCells);
        grid.addSpecialCells(specialCells);
        
        // Set random start and destination cells
        setRandomStartAndDestination();
        
        score = 0;
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
            // Calculate word score
            int wordScore = dictionary.getWordScore(wordStr);
            
            // Add path length bonus
            List<Cell> shortestPath = grid.findShortestPath(currentPath.get(0), 
                                                           currentPath.get(currentPath.size() - 1));
            if (shortestPath != null && shortestPath.size() == currentPath.size()) {
                wordScore += 50;  // Bonus for using optimal path
            }

            // Add special cell bonus
            for (Cell cell : currentPath) {
                if (cell.isSpecial()) {
                    wordScore += 25;  // Bonus for each special cell used
                }
            }

            score += wordScore;
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
        return movesLeft <= 0 && !hasWon();
    }

    // Getters
    public Grid getGrid() { return grid; }
    public int getScore() { return score; }
    public List<String> getFoundWords() { return new ArrayList<>(foundWords); }
    public int getMovesLeft() { return movesLeft; }
    public int getRequiredWords() { return requiredWords; }
    public List<Cell> getCurrentPath() { return new ArrayList<>(currentPath); }
    public List<List<Cell>> getPreviousPaths() {
        return previousPaths;
    }
}
