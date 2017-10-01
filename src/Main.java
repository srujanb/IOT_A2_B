import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
//import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class Main {
	
	static DecimalFormat decimalFormat;
	
	static SummaryStatistics statsD;
	static SummaryStatistics statsT;
	static SummaryStatistics statsP;

    static Queue<JobResults> s = new LinkedList<>();

    static double level = 0.95;

    static String finalString = "";

    public static void main(String args[]){
    	
    	 decimalFormat = new DecimalFormat("##00.0000");

        for (int i = 11; i <= 17; i++) {
            for (int j = 0; j < 50; j++) {
                JobTask.service_completion_time = i;
                JobTask task = new JobTask();
                task.begin();
            }
        }

        float superd = 0;
        float superd95 = 0;
        float supert = 0;
        float supert95 = 0;
        float superp = 0;

        System.out.println("\n\n\t\tMean T\t\t95th of T\tCI of T\t\t\t\tMean D\t\t95th of D\tCI of D\t\t\t\tP\t\t\tCI of P");
        finalString += "\n\nMean T\t95th of T\tMean D\t95th of D\tP";
        finalString += "\n";
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        finalString += "--------------------------------------------------------------------------------------------------------------------------------------------------------------------";
        finalString += "\n";

        for (int i = 0; i < 7; i++){
            float d = 0;
            float d95 = 0;
            float t = 0;
            float t95 = 0;
            float p = 0;

            //CI begin
            float CId = 0;
            float CIt = 0;
            float CIp = 0;
            // CI end
            
            System.out.print("\nFor S = " + (11 + i));
            finalString += "For S = " + (11 + i);
//            finalString += "\n";
            
            statsD = new SummaryStatistics();
            statsT = new SummaryStatistics();
            statsP = new SummaryStatistics();
            
            for (int j = 0; j < 50; j++){
                JobResults current = s.remove();
                d += current.getD();
                d95 += current.getD95();
                t += current.getT();
                t95 += current.getT95();
                p += current.getP();

                	// CI begin
                statsD.addValue(current.d);
                statsT.addValue(current.t);
                statsP.addValue(current.p);
                // CI end


            }

            superd += d/50;
            superd95 += d95/50;
            supert += t/50;
            supert95 += t95/50;
            superp += p/50;

            
           //start of conf int
            double ci = calcMeanCI(statsT, 0.95);
//            System.out.println(String.format("Mean: %f", statsT.getMean()));
            double lower = statsT.getMean() - ci;
            double upper = statsT.getMean() + ci;
//            System.out.println(String.format("Confidence Interval 95%%: %f, %f", lower, upper));
            
            double ci2 = calcMeanCI(statsD, 0.95);
//            System.out.println(String.format("Mean: %f", statsD.getMean()));
            double lower2 = statsD.getMean() - ci2;
            double upper2 = statsD.getMean() + ci2;
//            System.out.println(String.format("Confidence Interval 95%%: %f, %f", lower2, upper2));
            
            double ci3 = calcMeanCI(statsP, 0.95);
//          System.out.println(String.format("Mean: %f", statsD.getMean()));
          double lower3 = statsD.getMean() - ci3;
          double upper3 = statsD.getMean() + ci3;
//          System.out.println(String.format("Confidence Interval 95%%: %f, %f", lower2, upper2));
            //end of conf int

//            System.out.println("\t" + t/50 + "\t\t" + t95/50 + "\t\t" + d/50 + "\t\t" + d95/50 + "\t\t" + p/50);
            System.out.println("\t" + decimalFormat.format(t/50) + "\t\t" + decimalFormat.format(t95/50) + "\t\t" + decimalFormat.format(lower) + " - " + decimalFormat.format(upper) + "\t\t" + decimalFormat.format(d/50) + "\t\t" + decimalFormat.format(d95/50) + "\t\t" +  decimalFormat.format(lower2) + " - " + decimalFormat.format(upper2) + "\t\t" + decimalFormat.format(p/50) + "\t\t" + decimalFormat.format(lower3) + " - " + decimalFormat.format(upper3));
//            finalString += "\t" + t/50 + "\t" + t95/50 + "\t" + d/50 + "\t" + d95/50 + "\t" + p/50;
            finalString += "\t" + decimalFormat.format(t/50) + "\t\t" + decimalFormat.format(t95/50) + "\t\t" + decimalFormat.format(lower) + " - " + decimalFormat.format(upper) + "\t\t" + decimalFormat.format(d/50) + "\t\t" + decimalFormat.format(d95/50) + "\t\t" +  decimalFormat.format(lower2) + " - " + decimalFormat.format(upper2) + "\t\t" + decimalFormat.format(p/50) + "\t\t" + decimalFormat.format(lower3) + " - " + decimalFormat.format(upper3);

        }
        
        System.out.println("\n\n");
        finalString += "\n\n";
        finalString += "\n";
        System.out.println("Mean of T : " + supert/7);
        finalString += "Mean of T : " + supert/7;
        finalString += "\n";
        System.out.println("95th of T : " + supert95/7);
        finalString += "95th of T : " + supert95/7;
        finalString += "\n";
        System.out.println("Mean of D : " + superd/7);
        finalString += "Mean of D : " + superd/7;
        finalString += "\n";
        System.out.println("95th of D : " + superd95/7);
        finalString += "95th of D : " + superd95/7;
        finalString += "\n";
        System.out.println("Mean of P : " + superp/7);
        finalString += "Mean of P : " + superp/7;
        finalString += "\n";
        System.out.println("\n\n");
//        System.out.println("statsD: " + statsD);
//        System.out.println("statsT: " + statsT.getVariance());        	

        // SAVE TO FILE
            try(  PrintWriter out = new PrintWriter( "output.txt" )  ){
                out.println( finalString );
//                System.out.println("File saved");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
    }
    
    private static double calcMeanCI(SummaryStatistics stats, double level) {
        try {
            // Create T Distribution with N-1 degrees of freedom
            TDistribution tDist = new TDistribution(stats.getN() - 1);
            // Calculate critical value
            double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
            // Calculate confidence interval
            return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
        } catch (MathIllegalArgumentException e) {
        		e.printStackTrace();
            return Double.NaN;
        }
    }


}
