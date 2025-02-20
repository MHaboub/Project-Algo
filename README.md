# Word Labyrinth Game

A grid-based word game where players navigate through a labyrinth of letters to form valid words.

## Features

- Three difficulty levels (Easy, Medium, Hard)
- Grid represented as a graph with various cell types:
  - Regular cells with letters
  - Blocked cells (obstacles)
  - Special cells (bonus points)
  - Start cell (green)
  - Destination cell (red)
- Scoring system based on:
  - Word length
  - Word difficulty
  - Path optimization
  - Special cell bonuses
- Interactive GUI with real-time feedback
- Word validation against difficulty-specific dictionaries

## Requirements

- Java Development Kit (JDK) 8 or higher
- Java Swing (included in JDK)

## How to Compile and Run

1. Navigate to the project directory:
```bash
cd "path/to/Project_Algo"
```

2. Create a 'bin' directory for compiled classes:
```bash
mkdir bin
```

3. Compile the source files:
```bash
javac -d bin src/**/*.java
```

4. Run the game:
```bash
java -cp bin ui.GameUI
```

## How to Play

1. Select a difficulty level when starting the game
2. Click on cells to form a path:
   - Start from the green cell (start position)
   - Move to adjacent cells (including diagonals)
   - Try to reach the red cell (destination)
3. Click "Submit Word" when you've formed a valid word
4. Try to find the required number of words before running out of moves
5. Use special cells (yellow) for bonus points
6. Avoid blocked cells (black)

## Game Rules

- Words must be valid according to the current difficulty's dictionary
- Each letter adds 10 base points to your score
- Medium difficulty words get a 50% point bonus
- Hard difficulty words get a 100% point bonus
- Using the shortest possible path adds a 50-point bonus
- Each special cell used adds a 25-point bonus
- You must find the required number of words and reach the destination cell to win
- The game ends when you run out of moves or reach the win condition

## Technical Details

The game implements several key algorithms and data structures:

- Graph representation of the grid
- Dijkstra's algorithm for shortest path finding
- Breadth-First Search (BFS) for path validation
- HashSet for efficient word validation
- Custom scoring system with multiple factors

## Project Structure

```
src/
├── model/
│   ├── Cell.java         # Represents a single cell in the grid
│   ├── Grid.java         # Manages the game grid and graph structure
│   └── Dictionary.java   # Handles word validation and difficulty levels
├── game/
│   └── WordLabyrinth.java # Main game logic and state management
└── ui/
    └── GameUI.java       # Graphical user interface
```
