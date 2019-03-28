/* Author: Jose Medrano
 * CSid: cs8bwajv
 * Date: 3/6/19
 * Sources:
 */
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;

/**
 * This class creates a RoundedSquare object to act as the player object with
 * it's own size.
 */
public class Player extends RoundedSquare {
  final static double STROKE_FRACTION = 0.1;

  /**
   * No-arg constructor that creates a default square with the fill color pink
   * and the stroke color blue.
   */
  public Player() { 
    setFill(Color.PINK);
    setStroke(Color.BLUE);
    setStrokeType(StrokeType.CENTERED);
  }

  /**
   * This method overrides the setSize from RoundedSquare and uses the player
   * variable STROKE_FRACTION to size it.
   * @param size is the amount of length for the square.
   */
  @Override
    public void setSize(double size) {
      setStrokeWidth(size*STROKE_FRACTION);
      super.setSize(size);
    }
}
