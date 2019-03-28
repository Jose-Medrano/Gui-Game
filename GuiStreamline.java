/* Author: Jose Medrano
 * CSid: cs8bwajv
 * Date: 3/6/19
 * Sources:
 */
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.animation.*;
import javafx.animation.PathTransition.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import javafx.util.Duration;

/**
 * This class creates a GUI version of the streamline game we made for our pa3.
 */
public class GuiStreamline extends Application {
  static final double SCENE_WIDTH = 500;
  static final double SCENE_HEIGHT = 600;
  static final String TITLE = "CSE 8b Streamline GUI";
  static final String USAGE = 
    "Usage: \n" + 
    "> java GuiStreamline               - to start a game with default" +
    " size 6*5 and random obstacles\n" + 
    "> java GuiStreamline <filename>    - to start a game by reading g" +
    "ame state from the specified file\n" +
    "> java GuiStreamline <directory>   - to start a game by reading a" +
    "ll game states from files in\n" +
    "                                     the specified directory and " +
    "playing them in order\n";

  static final Color TRAIL_COLOR = Color.DARKORANGE;
  static final Color GOAL_COLOR = Color.DARKSALMON;
  static final Color OBSTACLE_COLOR = Color.DIMGRAY;

  // Trail radius will be set to this fraction of the size of a board square.
  static final double TRAIL_RADIUS_FRACTION = 0.1;

  // Squares will be resized to this fraction of the size of a board square.
  static final double SQUARE_FRACTION = 0.8;

  Scene mainScene;
  Group levelGroup;                   // For obstacles and trails
  Group rootGroup;                    // Parent group for everything else
  Player playerRect;                  // GUI representation of the player
  RoundedSquare goalRect;             // GUI representation of the goal

  Shape[][] grid;                     // Same dimensions as the game board

  Streamline game;                    // The current level
  ArrayList<Streamline> nextGames;    // Future levels

  MyKeyHandler myKeyHandler;          // for keyboard input

  /**
   * Gets the width of our game board.
   * @return int width of the board for the current level.
   */
  public int getBoardWidth() {
    return game.currentState.board[0].length;
  }

  /**
   * Gets the heigh of the board game.
   * @return int height of the game.
   */
  public int getBoardHeight() {
    return game.currentState.board.length;  
  }

  /**
   * This method determines the maximum length of the inner squares based on the
   * dimensions of the board and the scene.
   * @return double is the max size of the squares.
   */
  public double getSquareSize() {
    double sceneHeight = mainScene.getHeight();
    double sceneWidth = mainScene.getWidth();

    double gHeight = getBoardHeight();
    double gWidth = getBoardWidth();

    double squareHeight = sceneHeight/gHeight;
    double squareWidth = sceneWidth/gWidth;

    if (squareHeight > squareWidth) {
      return squareWidth;

    } else {
      return squareHeight;
    }
  }

  /**
   * Destroy and recreate grid and all trail and obstacle shapes and assumes the 
   * dimensions of the board may have changed.
   */
  public void resetGrid() {
    levelGroup.getChildren().clear();
    grid = new Shape[getBoardHeight()][getBoardWidth()];
    for (int i = 0; i < getBoardHeight(); i++) {
      for (int j = 0; j < getBoardWidth(); j++) {
        double[] shapePos = boardIdxToScenePos(j,i);
        if (game.currentState.board[i][j] == GameState.TRAIL_CHAR ||
            game.currentState.board[i][j] == GameState.SPACE_CHAR) {
          grid[i][j] = new Circle(shapePos[0],shapePos[1],
              (getSquareSize())*TRAIL_RADIUS_FRACTION);
          levelGroup.getChildren().add(grid[i][j]);

        } else if (game.currentState.board[i][j] == GameState.OBSTACLE_CHAR){
          RoundedSquare obstacle = new RoundedSquare(shapePos[0],shapePos[1],
              getSquareSize()*SQUARE_FRACTION);
          obstacle.setFill(OBSTACLE_COLOR);
          grid[i][j] = obstacle;
          levelGroup.getChildren().add(grid[i][j]);

        }
      }
    }
    updateTrailColors();
  }

  /**
   * Sets the fill color of all trail Circles making them visible or not 
   * depending on if that board position equals TRAIL_CHAR.
   */
  public void updateTrailColors() {
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[0].length; j++) {
        if (grid[i][j] instanceof Circle) {
          if (game.currentState.board[i][j] == GameState.TRAIL_CHAR) {
            grid[i][j].setFill(TRAIL_COLOR);
          } else {
            grid[i][j].setFill(Color.TRANSPARENT);
          }
        }
      }
    }
  }

  /** 
   * Coverts the given board column and row into scene coordinates.
   * Gives the center of the corresponding tile.
   * 
   * @param boardCol a board column to be converted to a scene x
   * @param boardRow a board row to be converted to a scene y
   * @return scene coordinates as length 2 array where index 0 is x
   */
  static final double MIDDLE_OFFSET = 0.5;
  public double[] boardIdxToScenePos (int boardCol, int boardRow) {
    double sceneX = ((boardCol + MIDDLE_OFFSET) * 
        (mainScene.getWidth() - 1)) / getBoardWidth();
    double sceneY = ((boardRow + MIDDLE_OFFSET) * 
        (mainScene.getHeight() - 1)) / getBoardHeight();
    return new double[]{sceneX, sceneY};
  }

  /** 
   * Makes trail markers visible and changes player position 
   * To be called when the user moved the player and the GUI needs to be 
   * updated to show the new position 
   * Parameters are the old position, new position, and whether it 
   * was an undo movement.
   */
  public void onPlayerMoved(int fromCol, int fromRow, int toCol, int toRow, 
      boolean isUndo)
  {
    // If the position is the same, just return
    if (fromCol == toCol && fromRow == toRow) {
      return;
    }

    updateTrailColors();

    double[] playerPos = boardIdxToScenePos(toCol, toRow); 
    playerRect.setCenterX(playerPos[0]);
    playerRect.setCenterY(playerPos[1]);

    if (game.currentState.levelPassed) {
      onLevelFinished();
    }
  }

  /**
   * To be called when a key is pressed.
   */
  void handleKeyCode(KeyCode keyCode) {
    int oldCol = game.currentState.playerCol;
    int oldRow = game.currentState.playerRow;
    switch (keyCode) {
      case UP:
        game.recordAndMove(Direction.UP);
        onPlayerMoved(oldCol, oldRow, game.currentState.playerCol,
            game.currentState.playerRow, false);
        break;

      case LEFT:
        game.recordAndMove(Direction.LEFT);
        onPlayerMoved(oldCol, oldRow, game.currentState.playerCol,
            game.currentState.playerRow, false);
        break;

      case DOWN:
        game.recordAndMove(Direction.DOWN);
        onPlayerMoved(oldCol, oldRow, game.currentState.playerCol,
            game.currentState.playerRow, false);
        break;

      case RIGHT:
        game.recordAndMove(Direction.RIGHT);
        onPlayerMoved(oldCol, oldRow, game.currentState.playerCol,
            game.currentState.playerRow, false);
        break;

      case U:
        game.undo();
        onPlayerMoved(oldCol, oldRow, game.currentState.playerCol,
            game.currentState.playerRow, false);

        break;

      case O:
        game.saveToFile();
        break;

      case Q:
        System.exit(0);

      default:
        System.out.println("Possible commands:\n w - up\n " + 
            "a - left\n s - down\n d - right\n u - undo\n " + 
            "q - quit level");
        break;
    }
  }

  /**
   *  This nested class handles keyboard input and calls handleKeyCode()
   */
  class MyKeyHandler implements EventHandler<KeyEvent> {
    
    /**
     * Activates the keyEvent.
     * @param e is the keyEvent that is pressed such as an arrow key.
     */
    @Override
      public void handle(KeyEvent e) {
        handleKeyCode(e.getCode());
      }
  }

  /** 
   * To be called whenever the UI needs to be completely redone to reflect
   * a new level
   */
  public void onLevelLoaded() {
    resetGrid();

    double squareSize = getSquareSize() * SQUARE_FRACTION;

    // Update the player position
    double[] playerPos = boardIdxToScenePos(
        game.currentState.playerCol, game.currentState.playerRow
        );
    playerRect.setSize(squareSize);
    playerRect.setCenterX(playerPos[0]);
    playerRect.setCenterY(playerPos[1]);

    double[] goalPos = boardIdxToScenePos(
        game.currentState.goalCol, game.currentState.goalRow);
    goalRect.setSize(squareSize);
    goalRect.setCenterX(goalPos[0]);
    goalRect.setCenterY(goalPos[1]);
  }

  /**
   * Called when the player reaches the goal
   * Shows the winning animation and loads the next level if there is one.
   */
  static final double SCALE_TIME = 175;  // milliseconds for scale animation
  static final double FADE_TIME = 250;   // milliseconds for fade animation
  static final double DOUBLE_MULTIPLIER = 2;
  public void onLevelFinished() {
    // Clone the goal rectangle and scale it up until it covers the screen

    // Clone the goal rectangle
    Rectangle animatedGoal = new Rectangle(
        goalRect.getX(),
        goalRect.getY(),
        goalRect.getWidth(),
        goalRect.getHeight()
        );
    animatedGoal.setFill(goalRect.getFill());

    // Add the clone to the scene
    List<Node> children = rootGroup.getChildren();
    children.add(children.indexOf(goalRect), animatedGoal);

    // Create the scale animation
    ScaleTransition st = new ScaleTransition(
        Duration.millis(SCALE_TIME), animatedGoal
        );
    st.setInterpolator(Interpolator.EASE_IN);

    // Scale enough to eventually cover the entire scene
    st.setByX(DOUBLE_MULTIPLIER * 
        mainScene.getWidth() / animatedGoal.getWidth());
    st.setByY(DOUBLE_MULTIPLIER * 
        mainScene.getHeight() / animatedGoal.getHeight());

    /*
     * This will be called after the scale animation finishes.
     * If there is no next level, quit. Otherwise switch to it and
     * fade out the animated cloned goal to reveal the new level.
     */
    st.setOnFinished(e1 -> {

        if (nextGames.isEmpty()) {
          System.exit(0);
        }

        // Update UI to the next level, but it won't be visible yet
        // because it's covered by the animated cloned goal
        game = nextGames.get(0);
        nextGames.remove(0);
        onLevelLoaded();

        FadeTransition transition = 
          new FadeTransition(Duration.millis(FADE_TIME), animatedGoal);        
        transition.setFromValue(1.0);
        transition.setToValue(0.0);
        transition.setOnFinished( e -> {
          children.remove(animatedGoal);
          });
        transition.play();
        });

    // Start the scale animation
    st.play();
  }

  /** 
   * Performs file IO to populate game and nextGames using filenames from
   * command line arguments.
   */
  public void loadLevels() {
    game = null;
    nextGames = new ArrayList<Streamline>();

    List<String> args = getParameters().getRaw();
    if (args.size() == 0) {
      System.out.println("Starting a default-sized random game...");
      game = new Streamline();
      return;
    }

    // at this point args.length == 1

    File file = new File(args.get(0));
    if (!file.exists()) {
      System.out.printf("File %s does not exist. Exiting...", 
          args.get(0));
      return;
    }

    // if is not a directory, read from the file and start the game
    if (!file.isDirectory()) {
      System.out.printf("Loading single game from file %s...\n", 
          args.get(0));
      game = new Streamline(args.get(0));
      return;
    }

    // file is a directory, walk the directory and load from all files
    File[] subfiles = file.listFiles();
    Arrays.sort(subfiles);
    for (int i=0; i<subfiles.length; i++) {
      File subfile = subfiles[i];

      // in case there's a directory in there, skip
      if (subfile.isDirectory()) continue;

      // assume all files are properly formatted games, 
      // create a new game for each file, and add it to nextGames
      System.out.printf("Loading game %d/%d from file %s...\n",
          i+1, subfiles.length, subfile.toString());
      nextGames.add(new Streamline(subfile.toString()));
    }

    // Switch to the first level
    game = nextGames.get(0);
    nextGames.remove(0);
  }

  /**
   * The main entry point for all JavaFX Applications
   * Initializes instance variables, creates the scene, and sets up the UI
   */
  @Override
    public void start(Stage primaryStage) throws Exception {
      // Populate game and nextGames
      loadLevels();

      // Initialize the scene and our groups
      rootGroup = new Group();
      mainScene = new Scene(rootGroup, SCENE_WIDTH, SCENE_HEIGHT, 
          Color.BLACK);
      levelGroup = new Group();
      rootGroup.getChildren().add(levelGroup);

      goalRect = new RoundedSquare(getSquareSize()*SQUARE_FRACTION);
      goalRect.setFill(GOAL_COLOR);
      rootGroup.getChildren().add(goalRect);

      playerRect = new Player();
      playerRect.setFill(Color.ORANGE);
      rootGroup.getChildren().add(playerRect);

      onLevelLoaded();
      myKeyHandler = new MyKeyHandler();
      mainScene.setOnKeyPressed(myKeyHandler);

      // Make the scene visible
      primaryStage.setTitle(TITLE);
      primaryStage.setScene(mainScene);
      primaryStage.setResizable(true);
      primaryStage.show();

      // Adding a keyhandler
     
    }

  /** 
   * Execution begins here, but at this point we don't have a UI yet
   * The only thing to do is call launch() which will eventually result in
   * start() above being called.
   */
  public static void main(String[] args) {
    if (args.length != 0 && args.length != 1) {
      System.out.print(USAGE);
      return;
    }

    launch(args);
  }
}
