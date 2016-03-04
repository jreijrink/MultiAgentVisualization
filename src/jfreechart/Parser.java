package jfreechart;

import jfreechart.object.TimeFrame;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser {
  public static int MAXNOBJ_LOCAL = 10;      /* as tracked by local tracker */
  public static int MAXNOBJ_GLOBAL = 12;      /* as tracked by global tracker */
  public static int NPATH_MAX =  50;      /* maximum length of the global path (waypoints) */
  public static int NOBST_TEAMMEMBERS_MAX = 5;       /* maximum number of obstacles per path of team members */
  public static int MAX_TURTLES = 7;
  public static int TRC_NUMBER_OF_TURTLES = 6;
  public static int MAX_ACTIVE_TURTLES = 6;
  public static int MAX_OPPONENTS = 10;      /* maximum number of opponents in worldmodel */
  public static int NAVOIDABLES = 50;      /* maximum amount of avoidables that can be found by vision */
  public static int NAVOIDABLES_FOR_STRATEGY = 50;      /* also defined in init_motion.m! */
  public static int MAXBALLS_OV = 10;      /* maximum number of candidate balls found by omnivision and send to tracker */
  public static int MAXBALLS_LRF = 10;      /* maximum number of candidate balls found by LRF-field and send to tracker*/
  public static int MAX_DEVPCS = 20;

  public List<TimeFrame> parse(String fileName ) throws Exception {
    String name = "replay";
    List<TimeFrame> frames = new ArrayList();
    
    try {
        MatFileReader mfr = new MatFileReader(fileName);
        
        MLStructure mlArrayRetrived = (MLStructure)mfr.getMLArray( name );
        MLDouble data = (MLDouble)mlArrayRetrived.getField("data");
        double[][] dataMatrix = data.getArray();

        if(dataMatrix.length > 0) {
          int rowOffest = 1;
          for (int column = 0; column < dataMatrix[0].length; column++) {
            double[] values = new double[dataMatrix.length];
            for (int row = rowOffest; row < dataMatrix.length; row++) {
              values[row- rowOffest] = dataMatrix[row][column];
            }
            
            System.out.printf("Processing frame %d/%d\n", column + 1, dataMatrix[0].length);
            TimeFrame frame = new TimeFrame(column, values);
            
            frames.add(frame);
          }
        }
                  
        return frames;
    } catch (Exception ex) {
      Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
      throw ex;
    }
  }
}
