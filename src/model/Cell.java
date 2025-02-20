package model;

/**
 * Represents a cell in the word labyrinth grid.
 */
public class Cell {
    private char letter;
    private int row;
    private int col;
    private boolean blocked;
    private boolean special;
    private boolean used;

    public Cell(char letter, int row, int col) {
        this.letter = letter;
        this.row = row;
        this.col = col;
        this.blocked = false;
        this.special = false;
        this.used = false;
    }

    // Getters and setters
    public char getLetter() { return letter; }
    public void setLetter(char letter) { this.letter = letter; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean isSpecial() { return special; }
    public void setSpecial(boolean special) { this.special = special; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    @Override
    public String toString() {
        return "(" + row + "," + col + "):" + letter;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cell)) return false;
        Cell other = (Cell) obj;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}
