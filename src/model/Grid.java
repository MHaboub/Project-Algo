package model;

import java.util.*;

/**
 * Represents the game grid as a graph structure.
 */
public class Grid {
    private Cell[][] cells;
    private int rows;
    private int cols;
    private Map<Cell, List<Cell>> graph;
    private Cell startCell;
    private Cell destinationCell;
    private Random random;
    private static final int MAX_PLACEMENT_ATTEMPTS = 100;
    private List<String> placedWords;
    private int requiredWords;

    public Grid(int rows, int cols, int requiredWords) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        this.random = new Random();
        this.requiredWords = requiredWords;
        this.placedWords = new ArrayList<>();
        initializeEmptyGrid();
        placeBlockedCells();
        placeStartAndDestinationCells();
        buildGraph();
    }

    private void initializeEmptyGrid() {
        // Initialize all cells as empty
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(' ', i, j);
            }
        }
    }

    private void placeBlockedCells() {
        int numBlocked = (rows * cols) / 10; // 10% of cells are blocked
        int placed = 0;

        while (placed < numBlocked) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);

            if (!cells[row][col].isBlocked()) {
                cells[row][col].setBlocked(true);
                cells[row][col].setLetter(' ');
                placed++;
            }
        }
    }

    private void placeStartAndDestinationCells() {
        // Place start cell
        do {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (!cells[row][col].isBlocked()) {
                startCell = cells[row][col];
                startCell.setSpecial(true);
                break;
            }
        } while (true);

        // Place destination cell (away from start)
        do {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            if (!cells[row][col].isBlocked() && 
                cells[row][col] != startCell &&
                Math.abs(row - startCell.getRow()) + Math.abs(col - startCell.getCol()) >= Math.max(rows, cols) / 2) {
                destinationCell = cells[row][col];
                destinationCell.setSpecial(true);
                destinationCell.setLetter(' ');
                break;
            }
        } while (true);
    }

    private void buildGraph() {
        // Define possible movements (including diagonals)
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        graph = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell currentCell = cells[i][j];
                List<Cell> neighbors = new ArrayList<>();

                for (int k = 0; k < dx.length; k++) {
                    int newRow = i + dx[k];
                    int newCol = j + dy[k];

                    if (isValidPosition(newRow, newCol) && !cells[newRow][newCol].isBlocked()) {
                        neighbors.add(cells[newRow][newCol]);
                    }
                }

                graph.put(currentCell, neighbors);
            }
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public List<Cell> getNeighbors(Cell cell) {
        return graph.getOrDefault(cell, new ArrayList<>());
    }

    public Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return cells[row][col];
        }
        return null;
    }

    public void setStartCell(Cell cell) {
        this.startCell = cell;
    }

    public void setDestinationCell(Cell cell) {
        this.destinationCell = cell;
    }

    public Cell getStartCell() {
        return startCell;
    }

    public Cell getDestinationCell() {
        return destinationCell;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void addBlockedCells(int count) {
        Random random = new Random();
        int added = 0;
        while (added < count) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            Cell cell = cells[row][col];
            if (!cell.isBlocked() && cell != startCell && cell != destinationCell) {
                cell.setBlocked(true);
                added++;
            }
        }
        // Rebuild graph to account for new blocked cells
        buildGraph();
    }

    public void addSpecialCells(int count) {
        Random random = new Random();
        int added = 0;
        while (added < count) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            Cell cell = cells[row][col];
            if (!cell.isBlocked() && !cell.isSpecial() && cell != startCell && cell != destinationCell) {
                cell.setSpecial(true);
                added++;
            }
        }
    }

    public List<Cell> findShortestPath(Cell start, Cell end) {
        if (start == null || end == null) return null;

        Map<Cell, Cell> parentMap = new HashMap<>();
        Map<Cell, Integer> distanceMap = new HashMap<>();
        PriorityQueue<Cell> queue = new PriorityQueue<>(
            (a, b) -> distanceMap.get(a) - distanceMap.get(b));

        // Initialize distances
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                distanceMap.put(cells[i][j], Integer.MAX_VALUE);
            }
        }

        distanceMap.put(start, 0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            if (current.equals(end)) break;

            for (Cell neighbor : getNeighbors(current)) {
                int newDist = distanceMap.get(current) + 1;
                if (newDist < distanceMap.get(neighbor)) {
                    distanceMap.put(neighbor, newDist);
                    parentMap.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        // Reconstruct path
        List<Cell> path = new ArrayList<>();
        Cell current = end;
        while (current != null) {
            path.add(0, current);
            current = parentMap.get(current);
        }

        return path.get(0).equals(start) ? path : null;
    }

    public void initializeGrid(Set<String> dictionary) {
        // Convert dictionary to list and sort by length (longer words first)
        List<String> wordList = new ArrayList<>(dictionary);
        Collections.sort(wordList, (a, b) -> b.length() - a.length());

        // Define all possible directions for word placement
        int[][] directions = {
            {0, 1},   // right
            {1, 0},   // down
            {1, 1},   // diagonal down-right
            {1, -1},  // diagonal down-left
            {-1, 1},  // diagonal up-right
            {-1, -1}, // diagonal up-left
            {-1, 0},  // up
            {0, -1}   // left
        };

        // Try to place exactly the required number of words
        int wordsPlaced = 0;
        List<String> availableWords = new ArrayList<>(wordList);
        Collections.shuffle(availableWords); // Randomize word selection

        while (wordsPlaced < requiredWords && !availableWords.isEmpty()) {
            String word = availableWords.remove(0);
            if (word.length() <= Math.min(rows, cols)) {
                boolean placed = false;
                for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS && !placed; attempt++) {
                    int startRow = random.nextInt(rows);
                    int startCol = random.nextInt(cols);
                    int[] direction = directions[random.nextInt(directions.length)];

                    if (canPlaceWord(word, startRow, startCol, direction[0], direction[1])) {
                        placeWord(word, startRow, startCol, direction[0], direction[1]);
                        placedWords.add(word);
                        wordsPlaced++;
                        placed = true;
                    }
                }
            }
        }

        // Fill remaining cells with random letters
        fillRemainingCells();
    }

    private boolean canPlaceWord(String word, int startRow, int startCol, int dRow, int dCol) {
        int endRow = startRow + (word.length() - 1) * dRow;
        int endCol = startCol + (word.length() - 1) * dCol;

        // Check if word fits within grid bounds
        if (endRow < 0 || endRow >= rows || endCol < 0 || endCol >= cols) {
            return false;
        }

        // Check if path crosses any blocked cells or destination cell
        for (int i = 0; i < word.length(); i++) {
            int r = startRow + i * dRow;
            int c = startCol + i * dCol;
            Cell cell = cells[r][c];
            
            if (cell.isBlocked() || cell == destinationCell || 
                (cell.getLetter() != ' ' && cell.getLetter() != word.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private void placeWord(String word, int startRow, int startCol, int dRow, int dCol) {
        for (int i = 0; i < word.length(); i++) {
            int r = startRow + i * dRow;
            int c = startCol + i * dCol;
            cells[r][c].setLetter(word.charAt(i));
        }
    }

    private void fillRemainingCells() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = cells[i][j];
                if (cell.getLetter() == ' ' && !cell.isBlocked() && cell != destinationCell) {
                    cell.setLetter(alphabet.charAt(random.nextInt(alphabet.length())));
                }
            }
        }
    }

    public List<String> getPlacedWords() {
        return new ArrayList<>(placedWords);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = cells[i][j];
                if (cell.isBlocked()) {
                    sb.append("# ");
                } else if (cell.isSpecial()) {
                    sb.append("*").append(cell.getLetter()).append(" ");
                } else {
                    sb.append(cell.getLetter()).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
