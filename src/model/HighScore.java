package model;

import java.io.*;
import java.util.*;

public class HighScore implements Serializable {
    private String playerName;
    private int score;
    private Date date;

    public HighScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        this.date = new Date();
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public Date getDate() { return date; }

    @Override
    public String toString() {
        return String.format("%s: %d points", playerName, score);
    }
}
