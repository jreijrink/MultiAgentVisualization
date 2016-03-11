package jfreechart;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jfreechart.object.ParameterMap;
import jfreechart.object.Turtle;
import jfreechart.settings.Configuration;

public class Parser {
  //public static int MAX_TURTLES = 7;
  //public static int MAX_OPPONENTS = 10;
    
  public List<Turtle> parse(String fileName ) throws Exception {
    String name = "replay";
    List<Turtle> turtles = new ArrayList();
    ParameterMap parameterMap = new ParameterMap();
    Configuration configuration = new Configuration();
    int mappingSize = parameterMap.GetMappingSize();
    
    try {
        MatFileReader mfr = new MatFileReader(fileName);
        
        MLStructure mlArrayRetrived = (MLStructure)mfr.getMLArray( name );
        MLDouble data = (MLDouble)mlArrayRetrived.getField("data");
        double[][] dataMatrix = data.getArray();
        System.out.printf("Completed data read");

        if(dataMatrix.length > 0) {
          
          int rowOffest = 1;
          
          for(int turtleID = 0; turtleID < configuration.MaxTurtles; turtleID++) {            
            System.out.printf("Processing turtle %d\n", turtleID);
            double[][] turtleData = Arrays.copyOfRange(dataMatrix, (mappingSize * turtleID) + rowOffest, (mappingSize * (turtleID + 1)) + rowOffest);
            
            Turtle turtle = new Turtle(turtleID, turtleData);
            turtles.add(turtle);
          }
          
          /*
          int rowOffest = 1;
          for (int column = 0; column < dataMatrix[0].length; column++) {
            double[] values = new double[dataMatrix.length];
            for (int row = rowOffest; row < dataMatrix.length; row++) {
              values[row- rowOffest] = dataMatrix[row][column];
            }
            
            System.out.printf("Processing frame %d/%d\n", column + 1, dataMatrix[0].length);
            TimeFrame frame = new TimeFrame(column, values);
            
            turtles.add(frame);
          }
          */
          
          
        }
                  
        return turtles;
    } catch (Exception ex) {
      Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
      throw ex;
    }
  }
}
