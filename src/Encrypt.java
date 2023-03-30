import java.io.IOException;
import java.util.StringTokenizer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import com.google.common.base.Charsets;

public class Encrypt{



  static public class ParagraphInputFormat extends FileInputFormat<LongWritable, Text> {



    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
         ParagraphRecordReader re = new ParagraphRecordReader();
            re.initialize(split, context);
            return re;


    }
  }



  static public class ParagraphRecordReader extends RecordReader<LongWritable, Text> {

    private LongWritable key;
    private Text value;
    private boolean endOfFile = false;
    private TaskAttemptContext context;
    LineRecordReader lineRecordReader;

    public ParagraphRecordReader(){

    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
          lineRecordReader = new LineRecordReader();
          lineRecordReader.initialize(split, context);
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (endOfFile) {
            return false;
        }
        StringBuilder paragraph = new StringBuilder();
        boolean foundParagraph = false;
        while (!foundParagraph) {
            if (!lineRecordReader.nextKeyValue()) {
                endOfFile = true;
                break;
            }
            String line = lineRecordReader.getCurrentValue().toString();
            if (line.isEmpty()) {
                key = new LongWritable(lineRecordReader.getCurrentKey().get());
                foundParagraph = true;
            } else {
                paragraph.append(line).append("\n");
            }
        }
        if (paragraph.length() > 0) {   
            value = new Text(paragraph.toString());
            return true;
        }
        return false;
    }

    @Override
    public LongWritable getCurrentKey() throws IOException, InterruptedException {
        return key;
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
        return value;
    }

    @Override
    public float getProgress() throws IOException, InterruptedException {
        return endOfFile ? 1.0f : 0.0f;
    }

    @Override
    public void close() throws IOException {
        lineRecordReader.close();
    }
}


  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{
        byte[] secretKey = "9mng65v8jf4lxn93nabf981m".getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "TripleDES");
        byte[] iv = "a76nb5h9".getBytes();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
           Cipher encryptCipher;
    
     @Override
     protected void setup(Mapper<Object, Text, Text, Text>.Context context)
   throws IOException, InterruptedException {
      try{
        this.encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        this.encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
      }
         catch(Exception e){
          
            e.printStackTrace();
          }
 }

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    public String encrypt(String value) {
         String result = "";
          try{
        
       
        StringTokenizer itr = new StringTokenizer(value);
        String firstToken = itr.nextToken();
        byte[] secretMessagesBytes = firstToken.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessagesBytes);
        String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        result = encodedMessage + "       ";
        
        while (itr.hasMoreTokens()) {
         String t = itr.nextToken(); 
         secretMessagesBytes = t.getBytes(StandardCharsets.UTF_8);
         encryptedMessageBytes = encryptCipher.doFinal(secretMessagesBytes);
         encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
         result = result +" "+ encodedMessage;
         
      }
          }
          catch(Exception e){
            result = "error" + e.getMessage();
            e.printStackTrace();
          }

        return result;
    }
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString(),"\n");

        String result = "";
         while (itr.hasMoreTokens()) {
          result = result + encrypt(itr.nextToken()) + "\n";
      }
      context.write(new Text(result),new Text(""));
    
     
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {

      Text res = new Text("");
      context.write(key, res);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "encrypt");
   
    job.setJarByClass(Encrypt.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setInputFormatClass(ParagraphInputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}