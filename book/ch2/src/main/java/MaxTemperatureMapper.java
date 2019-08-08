import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * input line -> Map {year, temperature} 변환
 *
 * @GitHub : https://github.com/zacscoding
 */
public class MaxTemperatureMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final int MISSING = 9999;

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();
        String year = getYear(line);

        int airTemperature = getAirTemperature(line);

        String quality = getQuality(line);

        if (airTemperature != MISSING && quality.matches("[01459]")) {
            context.write(new Text(year), new IntWritable(airTemperature));
        }
    }

    private static String getYear(String line) {
        return line.substring(15, 19);
    }

    private static int getAirTemperature(String line) {
        if (line.charAt(87) == '+') {
            return Integer.parseInt(line.substring(88, 92));
        }

        return Integer.parseInt(line.substring(87, 92));
    }

    private static String getQuality(String line) {
        return line.substring(92, 93);
    }

//    public static void main(String[] args) {
//        String[] lines = {
//                "0067011990999991950051507004+68750+023550FM-12+038299999V0203301N00671220001CN9999999N9+00001+99999999999",
//                "0043011990999991950051512004+68750+023550FM-12+038299999V0203201N00671220001CN9999999N9+00221+99999999999",
//                "0043011990999991950051518004+68750+023550FM-12+038299999V0203201N00261220001CN9999999N9-00111+99999999999",
//                "0043012650999991949032412004+62300+010750FM-12+048599999V0202701N00461220001CN0500001N9+01111+99999999999",
//                "0043012650999991949032418004+62300+010750FM-12+048599999V0202701N00461220001CN0500001N9+00781+99999999999"
//        };
//
//        int idx = 0;
//        for (String line : lines) {
//            String year = getYear(line);
//            int temp = getAirTemperature(line);
//            String quality = getQuality(line);
//            System.out.println(String.format("[%d] year : %s | tempo : %d | quality : %s"
//                    , idx++, year, temp, quality));
//        }
//
//        // Output
////        [0] year : 1950 | tempo : 0 | quality : 1
////        [1] year : 1950 | tempo : 22 | quality : 1
////        [2] year : 1950 | tempo : -11 | quality : 1
////        [3] year : 1949 | tempo : 111 | quality : 1
////        [4] year : 1949 | tempo : 78 | quality : 1
//    }
}
