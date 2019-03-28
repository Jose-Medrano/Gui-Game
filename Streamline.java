/** 
 * Author: Jose Medrano
 * CSid: cs8bwajv
 * Date: 2/13/19
 * Sources
 */

import java.util.*;
import java.io.*;

/** 
 * THis streamline object contains a GameState object and an array of
 * GameState objects so that we may be able to play the game by holding old
 * GameStates within the array.
 * */
public class Streamline {

  final static int DEFAULT_HEIGHT = 6;
  final static int DEFAULT_WIDTH = 5;

  final static String OUTFILE_NAME = "saved_streamline_game";                

  GameState currentState;
  List<GameState> previousStates;

  /**
   * This first constructor initializes a GameState object and an empty
   * ArrayList of GameStates.
   */
  public Streamline() {
    currentState = new GameState(DEFAULT_HEIGHT, DEFAULT_WIDTH, DEFAULT_HEIGHT,
        0, 0, DEFAULT_WIDTH);
    currentState.playerRow = DEFAULT_HEIGHT - 1;
    currentState.playerCol = 0;
    currentState.goalRow = 0;
    currentState.goalCol = DEFAULT_WIDTH - 1;
    currentState.addRandomObstacles(3);
    previousStates = new ArrayList<GameState>();
  }

  /**
   * This constructor reads from a file and loads in a saved GameState.
   */
  public Streamline(String filename) {
    try {
      loadFromFile(filename);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method reads the input from a file and creates a Streamline object
   * based on the contents within the file read.
   * @param filename is the file that we are reading from.
   */
  protected void loadFromFile(String filename) throws IOException {            
    Scanner content = new Scanner(new File(filename));
    int height = content.nextInt();
    int width = content.nextInt();
    int pRow = content.nextInt();
    int pCol = content.nextInt();
    int gRow = content.nextInt();
    int gCol = content.nextInt();
    content.nextLine();

    GameState oldGame = new GameState(height, width, pRow, pCol, gRow, gCol);
    previousStates = new ArrayList<GameState>();
    currentState = oldGame;
    if (currentState.playerRow == currentState.goalRow && 
        currentState.playerCol == currentState.goalCol) {
      currentState.levelPassed = true;
    }

    char[][] oldBoard = new char[height][width];
    //content.nextLine();
    int placer = 0;
    while (content.hasNext()) {
      String board = content.nextLine();
      for (int i = 0; i < width; i++) {
        currentState.board[placer][i] = board.charAt(i);
      }
      placer++;
    }      
  }

  /**
   * This method saves the current GameState into the array of previous
   * GameStates(if the current one changes) and then moves the player in 
   * the given direction.
   * @param direction is the enum direction we want the player to move in.
   */
  void recordAndMove(Direction direction) {
    if (direction == null) {
      return;
    }
    GameState previous = new GameState(currentState);
    currentState.move(direction);

    if (!(currentState.equals(previous))) {
      previousStates.add(previous);
    }
  } 

  /**
   * This method changes the current GameState into the  previous GameState 
   * if there is one.
   */
  void undo() {
    if (previousStates.size() == 0) {
      return;
    }
    int lastState = previousStates.size() - 1;
    currentState = previousStates.get(lastState);
    previousStates.remove(lastState);
  }

  /**
   * This method is what allows the user to interact with the board; we have a
   * while loop that runs until the user wins.
   */
  void play() {
    while (currentState.levelPassed != true) {
      System.out.println(currentState.toString());
      System.out.print("> ");
      Scanner input = new Scanner(System.in);
      String move = input.next();

      switch (move) {
        case "w":
          recordAndMove(Direction.UP);
          break;

        case "a":
          recordAndMove(Direction.LEFT);
          break;

        case "s":
          recordAndMove(Direction.DOWN);
          break;

        case "d":
          recordAndMove(Direction.RIGHT);
          break;

        case "u":
          undo();
          break;

        case "o":
          saveToFile();
          break;

        case "q":
          return;
      }
      if (currentState.levelPassed) {
        System.out.println(currentState.toString()); 
        System.out.println("Level Passed!");
      }
    }
  }

  /**
   * This method saves the game in it's current state for it to be loaded     
   * later. 
   */
  void saveToFile() {
    try {
      PrintWriter saveFile = new PrintWriter(new File(OUTFILE_NAME));
      saveFile.write(currentState.board.length + " " + 
          currentState.board[0].length + "\n");
      saveFile.write(currentState.playerRow + " " + currentState.playerCol +
          "\n");
      saveFile.write(currentState.goalRow + " " + currentState.goalCol + "\n");

      for (int i = 0; i < currentState.board.length; i++) {
        StringBuilder line = new StringBuilder();

        for (int j = 0; j < currentState.board[0].length; j++) {
          line.append(currentState.board[i][j]);  
        }
        saveFile.write(line.toString() + "\n");
      }
      saveFile.close();
      System.out.println("Saved current state to: saved_streamline_game");
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
}
