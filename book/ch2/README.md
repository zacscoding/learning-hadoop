## CHAPTER 2 맵리듀스  

### Index  

- <a href="#install_hadoop">하둡 설치</a> 
- <a href="#map_reduce_java">자바 맵리듀스 예제</a>  

---  

<div id="install_hadoop"></div>

### 하둡 설치  

#### 설치

> download  

```aidl
https://hadoop.apache.org/releases.html
```  

> untar  

```aidl
~/infra/hadoop$ tar -xvf hadoop-3.2.0
```  

> environment  

```aidl
export HADOOP_HOME=~/infra/hadoop/hadoop-3.2.0
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```  

> check  

```aidl
~/infra/hadoop/hadoop-3.2.0$ hadoop version
Hadoop 3.2.0
Source code repository https://github.com/apache/hadoop.git -r e97acb3bd8f3befd27418996fa5d4b50bf2e17bf
Compiled by sunilg on 2019-01-08T06:08Z
Compiled with protoc 2.5.0
From source with checksum d3f0795ed0d9dc378e2c785d3668f39
This command was run using /home/zaccoding/infra/hadoop/hadoop-3.2.0/share/hadoop/common/hadoop-common-3.2.0.jar
```

#### 환경 설정  

- 공통 속성은 core-site.xml | hdfs-site.xml, mapred-site.xml, yarn-site.xml  
과 같이 속성과 관련된 명칭의 파일을 이용(etc/hadoop)  
- share/doc 하위에 core-default.xml 등과 같이 기본 속성 값을 확인 가능

> 하둡은 세 가지 모드 중 하나로 동작  

- **독립(로컬)모드**  
; 데몬이 실행되지 않고 모든 것이 단독 JVM 내에서 실행  
시험과 디버깅을 쉽게 할 수 있어서 개발 단계에서 적합  

- **의사분산 모드**  
; 모든 하둡 데몬은 로컬 머신에서 실행  
=> 작은 규모의 클러스터에서 실행하는 것과 같은 효과  

- **완전 분산 모드**  
; 하둡 데몬을 여러 대의 머신으로 구성된 클러스터에서 실행  

> 각 모드의 주요 속성 설정  

<table>
    <tr>
        <td>구성요소</td> <td>속성</td> <td>독립</td> 
        <td>의사분산</td> <td>완전분산</td>
    </tr>
    <tr>
        <td>공통</td>
        <td>
            Fs.defaultFS
        </td>
        <td>
            file:///(기본)
        </td>
        <td>
            hdfs://localhost/
        </td>
        <td>
            hdfs://namenode/
        </td>
    </tr>
    <tr>
        <td>HDFS</td>
        <td>
            dfs.replication
        </td>            
        <td>
            N/A        
        </td>
        <td>
            1
        </td>
        <td>
            3(기본)
        </td>
    </tr>
    <tr>
        <td>맵리듀스</td>
        <td>
            mapreduce.framework.name
        </td>
        <td>
            local(기본)
        </td>
        <td>
            yarn
        </td>
        <td>
            yarn
        </td>
    </tr>
    <tr>
        <td>YARN</td>
        <td>
            yarn.resourcemanager.hostname            
        </td>
        <td>
            N/A
        </td>
        <td>
            localhost
        </td>
        <td>
            resourcemanager
        </td>
    </tr>
    <tr>
        <td>YARN</td>
        <td>
            yarn.nodemanager.aux-services
        </td>
        <td>
            N/A
        </td>
        <td>
            mapreduce_shuffle
        </td>
        <td>
            mapreduce_shuffle
        </td>
    </tr>
</table> 

---  

<div id="map_reduce_java"></div>  

> input  

```aidl
0067011990999991950051507004+68750+023550FM-12+038299999V0203301N00671220001CN9999999N9+00001+99999999999
0043011990999991950051512004+68750+023550FM-12+038299999V0203201N00671220001CN9999999N9+00221+99999999999
0043011990999991950051518004+68750+023550FM-12+038299999V0203201N00261220001CN9999999N9-00111+99999999999
0043012650999991949032412004+62300+010750FM-12+048599999V0202701N00461220001CN0500001N9+01111+99999999999
0043012650999991949032418004+62300+010750FM-12+048599999V0202701N00461220001CN0500001N9+00781+99999999999
```  


> Mapper  

```aidl
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
}
```  

> Reducer  

```aidl
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Mapper {year, temperature} -> Reducer {year, max value}
 * @GitHub : https://github.com/zacscoding
 */
public class MaxTemperatureReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {

        int maxValue = Integer.MIN_VALUE;

        for (IntWritable value : values) {
            maxValue = Math.max(maxValue, value.get());
        }

        context.write(key, new IntWritable(maxValue));
    }
}
```  

> Main  

```aidl
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;

/**
 *
 * @GitHub : https://github.com/zacscoding
 */
public class MaxTemperature {

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: MaxTemperature <input path> <output path>");
            System.exit(-1);
        }

        Job job = Job.getInstance();
        job.setJarByClass(MaxTemperature.class);
        job.setJobName("Max temperature");

        FileInputFormat.addInputPath((JobConf) job.getConfiguration(), new Path(args[0]));
        FileOutputFormat.setOutputPath((JobConf) job.getConfiguration(), new Path(args[1]));

        job.setMapperClass(MaxTemperatureMapper.class);
        job.setReducerClass(MaxTemperatureReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
```  



  

  
