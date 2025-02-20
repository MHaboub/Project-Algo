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
    private List<String> placedWords;

    public Grid(int rows, int cols, Set<String> dictionary) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        this.graph = new HashMap<>();
        this.placedWords = new ArrayList<>();
        initializeGrid(dictionary);
        buildGraph();
    }

    private void initializeGrid(Set<String> dictionary) {
        // First, fill the grid with empty cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(' ', i, j);
            }
        }

        // Convert dictionary to list for random access
        List<String> wordList = new ArrayList<>(dictionary);
        Collections.shuffle(wordList);

        // Try to place words in random directions
        Random random = new Random();
        int wordsToPlace = Math.min(wordList.size(), (rows * cols) / 4); // Place about 25% of grid size in words

        for (int i = 0; i < wordsToPlace && i < wordList.size(); i++) {
            String word = wordList.get(i).toUpperCase();
            boolean placed = false;
            int attempts = 0;
            
            while (!placed && attempts < 50) { // Limit attempts per word
                // Random starting position
                int startRow = random.nextInt(rows);
                int startCol = random.nextInt(cols);
                
                // Random direction (-1, 0, 1 for each dimension)
                int rowDir = random.nextInt(3) - 1;
                int colDir = random.nextInt(3) - 1;
                
                if (rowDir == 0 && colDir == 0) continue; // Skip if no direction
                
                placed = tryPlaceWord(word, startRow, startCol, rowDir, colDir);
                attempts++;
            }
            
            if (placed) {
                placedWords.add(word);
            }
        }

        // Fill remaining empty cells with random letters
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].getLetter() == ' ') {
                    char randomLetter = alphabet.charAt(random.nextInt(alphabet.length()));
                    cells[i][j].setLetter(randomLetter);
                }
            }
        }
    }

    private boolean tryPlaceWord(String word, int startRow, int startCol, int rowDir, int colDir) {
        // Check if word fits
        int endRow = startRow + (word.length() - 1) * rowDir;
        int endCol = startCol + (word.length() - 1) * colDir;
        
        if (endRow < 0 || endRow >= rows || endCol < 0 || endCol >= cols) {
            return false;
        }

        // Check if path is clear or compatible
        for (int i = 0; i < word.length(); i++) {
            int currentRow = startRow + i * rowDir;
            int currentCol = startCol + i * colDir;
            char currentCell = cells[currentRow][currentCol].getLetter();
            
            if (currentCell != ' ' && currentCell != word.charAt(i)) {
                return false;
            }
        }

        // Place the word
        for (int i = 0; i < word.length(); i++) {
            int currentRow = startRow + i * rowDir;
            int currentCol = startCol + i * colDir;
            cells[currentRow][currentCol].setLetter(word.charAt(i));
        }

        return true;
    }

    private void buildGraph() {
        // Define possible movements (including diagonals)
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

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

    public List<String> getPlacedWords() {
        return new ArrayList<>(placedWords);
    }
}
