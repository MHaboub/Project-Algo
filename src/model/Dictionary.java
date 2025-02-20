package model;

import java.io.*;
import java.util.*;

/**
 * Manages the word dictionaries for different difficulty levels.
 */
public class Dictionary {
    private Set<String> easyWords;
    private Set<String> mediumWords;
    private Set<String> hardWords;
    private DifficultyLevel currentLevel;

    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }

    public Dictionary() {
        easyWords = new HashSet<>();
        mediumWords = new HashSet<>();
        hardWords = new HashSet<>();
        currentLevel = DifficultyLevel.EASY;
        
        // Initialize with some sample words for each difficulty
        initializeDefaultWords();
    }

    private void initializeDefaultWords() {
        // Easy words (3-4 letters)
        String[] easy = {"cat", "dog", "run", "jump", "play", "ball", "home", "tree",
                        "book", "fish", "bird", "walk", "talk", "sing", "food"};
        easyWords.addAll(Arrays.asList(easy));

        // Medium words (5-7 letters)
        String[] medium = {"garden", "flower", "window", "pencil", "school", "friend",
                          "purple", "orange", "yellow", "monkey", "rabbit", "turtle"};
        mediumWords.addAll(Arrays.asList(medium));

        // Hard words (8+ letters)
        String[] hard = {"elephant", "butterfly", "chocolate", "adventure", "beautiful",
                        "dangerous", "wonderful", "knowledge", "important", "different"};
        hardWords.addAll(Arrays.asList(hard));
    }

    public void setDifficultyLevel(DifficultyLevel level) {
        this.currentLevel = level;
    }

    public DifficultyLevel getCurrentLevel() {
        return currentLevel;
    }

    public boolean isValidWord(String word) {
        word = word.toLowerCase();
        switch (currentLevel) {
            case EASY:
                return easyWords.contains(word);
            case MEDIUM:
                return mediumWords.contains(word);
            case HARD:
                return hardWords.contains(word);
            default:
                return false;
        }
    }

    public Set<String> getCurrentDictionary() {
        switch (currentLevel) {
            case EASY:
                return new HashSet<>(easyWords);
            case MEDIUM:
                return new HashSet<>(mediumWords);
            case HARD:
                return new HashSet<>(hardWords);
            default:
                return new HashSet<>();
        }
    }

    public void addWord(String word, DifficultyLevel level) {
        word = word.toLowerCase();
        switch (level) {
            case EASY:
                easyWords.add(word);
                break;
            case MEDIUM:
                mediumWords.add(word);
                break;
            case HARD:
                hardWords.add(word);
                break;
        }
    }

    public int getWordScore(String word) {
        if (!isValidWord(word)) return 0;
        
        int baseScore = word.length() * 10;  // 10 points per letter
        
        // Difficulty multiplier
        switch (currentLevel) {
            case EASY:
                return baseScore;
            case MEDIUM:
                return (int)(baseScore * 1.5);  // 50% bonus for medium words
            case HARD:
                return baseScore * 2;  // 100% bonus for hard words
            default:
                return 0;
        }
    }
}
