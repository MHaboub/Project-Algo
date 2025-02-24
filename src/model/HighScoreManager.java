package model;

import java.io.*;
import java.util.*;

public class HighScoreManager {
    private static final String SCORES_FILE = "highscores.dat";
    private List<HighScore> highScores;
    private static final int MAX_HIGH_SCORES = 3;

    public HighScoreManager() {
        highScores = new ArrayList<>();
        loadHighScores();
    }

    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_HIGH_SCORES) return true;
        return score > highScores.get(highScores.size() - 1).getScore();
    }

    public void addScore(String playerName, int score) {
        highScores.add(new HighScore(playerName, score));
        Collections.sort(highScores, (a, b) -> b.getScore() - a.getScore());
        
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = highScores.subList(0, MAX_HIGH_SCORES);
        }
        
        saveHighScores();
    }

    public List<HighScore> getHighScores() {
        return new ArrayList<>(highScores);
    }

    public String getHighScoresText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < highScores.size(); i++) {
            HighScore score = highScores.get(i);
            sb.append(String.format("%d. %s: %d\n", i + 1, score.getName(), score.getScore()));
        }
        return sb.toString();
    }

    private void loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            highScores = (List<HighScore>) ois.readObject();
        } catch (Exception e) {
            highScores = new ArrayList<>();
        }
    }

    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
