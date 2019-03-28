/** 
 * Author: Jose Medrano
 * CSid: cs8bwajv
 * Date: 2/6/19
 * Sources:
 */

import java.util.*;
import java.util.Random;

/** 
 * This class creates a gamestate that is a char 2d array that we use to play
 * games with.
 */
public class GameState {

  // Used to populate char[][] board below and to display the
  // current state of play.
  final static char TRAIL_CHAR = '.';
  final static char OBSTACLE_CHAR = 'X';
  final static char SPACE_CHAR = ' ';
  final static char CURRENT_CHAR = 'O';
  final static char GOAL_CHAR = '@';
  final static char NEWLINE_CHAR = '\n';

  // This represents a 2D map of the board
  char[][] board;

  // Location of the player
  int playerRow;
  int playerCol;

  // Location of the goal
  int goalRow;
  int goalCol;

  // true means the player completed this level
  boolean levelPassed;

  /**
   * This constructor takes in multiple parameters to create a GameState object.
   * @param height is the amount of rows the 2D array will have.
   * @param width is the amount of columns the 2D array will have.
   * @param playerRow is the location of which row the player is currently in.
   * @param playerCol is the location of which column the player is in.
   * @param goalRow is the location of which row the goal is in.
   * @param goalCol is the location of which column the goal is in.
   */
  public GameState(int height, int width, int playerRow, int playerCol, 
      int goalRow, int goalCol) {

    board = new char[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        board[i][j] = SPACE_CHAR;
      }
    } 

    this.playerRow = playerRow;
    this.playerCol = playerCol;
    this.goalRow = goalRow;
    this.goalCol = goalCol;

    /*board = new char[height][width];
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        board[i][j] = SPACE_CHAR;
      }
    }*/ 

  }

  /**
   * This constructor passes in a GameState object and creates an identical
   * copy.
   * @param other is the GameState object that we are trying to copy.
   */
  public GameState(GameState other) {
    playerRow = other.playerRow;
    playerCol = other.playerCol;
    goalRow = other.goalRow;
    goalCol = other.goalCol;
    levelPassed = other.levelPassed;
    board = new char[other.board.length][other.board[0].length];

    for (int i = 0; i < other.board.length; i++) {
      for (int j = 0; j < other.board[i].length; j++) {
        board[i][j] = other.board[i][j];
      }
    }
  }

  /**
   * This method adds obstacles in random location of the 2D array.
   * @param count is the amount of objects you want to add.
   */
  void addRandomObstacles(int count) {
    int spaceChecker = 0;
    int placed = 0;

    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        if ((i != playerRow || j != playerCol) &&
            (i != goalRow || j != goalCol)) {

          spaceChecker++;
        }
      }
    }

    if (count > spaceChecker || count < 0) {
      return;
    }

    while (placed < count) {
      Random random = new Random();
      int randCol = random.nextInt(board[0].length);
      int randRow = random.nextInt(board.length);

      if ((randRow != playerRow || randCol != playerCol) &&
          (randRow != goalRow || randCol != goalCol)) {
                
        if (board[randRow][randCol] == SPACE_CHAR) {
          board[randRow][randCol] = OBSTACLE_CHAR;
          placed++;
        }
      }
    }
  }

  /**
   * This method rotates the values of an array clockwise, which may also resize
   * the entire 2D array.
   */
  void rotateClockwise() {
    char[][] copyArray = new char[this.board[0].length][this.board.length];

    for (int i = 0; i < copyArray.length; i++) {
      for (int j = 0; j < copyArray[0].length; j++) {
        copyArray[i][j] = this.board[this.board.length - j - 1][i];
      }
    }
    int tmp = playerRow;
    playerRow = playerCol;
    playerCol = board.length - tmp - 1;

    int tmpTwo = goalRow;
    goalRow = goalCol;
    goalCol = board.length - tmpTwo - 1;
    this.board = copyArray;
  }

  /**
   * This method moves the player towards the right and stops if the player hits
   * their own trail, if they win, or if they hit the obstacle.
   */
  void moveRight() {
    while (playerCol < board[0].length - 1) {
      board[playerRow][playerCol] = TRAIL_CHAR;
      playerCol++;

      if (playerRow == goalRow && playerCol == goalCol) {
        levelPassed = true;
        return;
      }

      if (board[playerRow][playerCol] == OBSTACLE_CHAR ||
          board[playerRow][playerCol] == TRAIL_CHAR) {
        playerCol--;
        board[playerRow][playerCol] = SPACE_CHAR;
        return;
      }
    }
  }

  /**
   * This method takes in four enum directions (up, down, left, right) and moves
   * the player in such a direction.
   * @param direction is the direction we want the player to move.
   */
  void move(Direction direction) {
    int leftOver = 4 - direction.getRotationCount();
    for (int i = 0; i < direction.getRotationCount(); i++) {
      this.rotateClockwise();
    }
    this.moveRight();

    for (int j = 0; j < leftOver; j++) {
      this.rotateClockwise();
    }

  }

  /**
   * This method compares a GameState object to a Object and determines if 
   * they are of type GameState.
   * @param other is the object we are comparing.
   * @return boolean is either true or false depending if the object is of
   * type GameState.
   */
  @Override
    // compare two game state objects, returns true if all fields match
    public boolean equals(Object other) {
      if (other instanceof GameState) {
        if (board == null || ((GameState)other).board == null ||
            ((GameState)other).playerRow != playerRow ||
            ((GameState)other).playerCol != playerCol ||
            ((GameState)other).goalRow != goalRow ||
            ((GameState)other).goalCol != goalCol ||
            ((GameState)other).levelPassed != levelPassed ||
            ((GameState)other).board.length != board.length ||
            ((GameState)other).board[0].length != board[0].length) {

          return false;
        }
      } else {
        return false;
      }

      for (int i = 0; i < board.length; i++) {
        for (int j = 0; j < board[0].length; j++) {
          if (((GameState)other).board[i][j] != board[i][j]) {
            return false;
          }
        }
      }

      return true;
    }

  /**
   * This method takes the char[][] and prints it out as a board and contains
   * it within a border.
   * @return String is the the board and the border.
   */
  @Override
    public String toString() {
      StringBuilder game = new StringBuilder();
      int topRow = 0;
      int botRow = 0;

      while(topRow < 2*this.board[0].length + 3) {
        game.append("-");
        topRow++;
      }

      game.append(NEWLINE_CHAR);
      for (int i = 0; i < this.board.length; i++) {
        game.append('|');

        for (int j = 0; j < this.board[i].length; j++) {

          if (i == this.playerRow && j == this.playerCol) {
            game.append(" " + CURRENT_CHAR);

          } else if (i == this.goalRow && j == this.goalCol) {
            game.append(" " + GOAL_CHAR);

          } else {
            game.append(" " + this.board[i][j]);
          }
        }

        game.append(" |");
        game.append(NEWLINE_CHAR);
      }

      while(botRow < 2*this.board[0].length + 3) {
        game.append("-");
        botRow++;
      }
      game.append(NEWLINE_CHAR);
      return game.toString();
    }

  public static void main(String[] args) {
    GameState test = new GameState(6, 5, 3, 0, 1, 2);
    
    test.addRandomObstacles(7);
    System.out.println(test.toString());

    test.rotateClockwise();
    System.out.println(test.toString());

    test.moveRight();
    System.out.println(test.toString());
    /*
    test.move(Direction.DOWN);
    System.out.println(test.toString());

    test.move(Direction.UP);
    System.out.println(test.toString());
*/

  }
}
