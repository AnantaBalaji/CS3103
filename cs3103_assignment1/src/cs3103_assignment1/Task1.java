/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;






/**
 *
 * @author plasmashadow
 */
public class Task1 {
    /*
     read the file the preprocess dataset
    */
    
    public List<Long> PreprocessPath(String aspath){
        
        String path = this.reducePath(aspath);
        String[] arr = path.split(" ");
        List<Long> resultList = new ArrayList<Long>();
        for (String piece: arr){
            try{
            resultList.add(Long.valueOf(piece));
            }
            catch(java.lang.NumberFormatException e){
               // System.out.println(piece);
            }
        }
        return resultList;
    }
    
    public String reducePath(String path){
      String[] str =  path.split("\\{.*}");
      StringBuilder result = new StringBuilder();
      for (String piece: str){
          result = result.append(piece);
      }
      return result.toString();
    }
    
    public static void main(String[] args) throws IOException{
        
        System.out.println(args.length);
        if(args.length < 1){
           System.out.println("Usage: Task1 <inputfilename>");
           return ;
        }
        Task1 tsk = new Task1();
        String file = args[0];
        FileReader reader = new FileReader(file);
        File output = new File("output.txt");
        BufferedReader filereader = new BufferedReader(reader);
        FileWriter filewriter = new FileWriter(output);
        
        
        
        try{
        String line = null;
        Pattern r = Pattern.compile("\\{.*}");
        
        while ((line = filereader.readLine() )!= null) {
//            sb.append(line);
//            sb.append(System.lineSeparator());
//            line = filereader.readLine();
            Matcher m = r.matcher(line);
            if(!m.find()){
            List<Long> resultlist = tsk.PreprocessPath(line);
            HashSet<Long> set = new LinkedHashSet<Long>(resultlist);
            System.out.println(set.toString());
            }
            
        }
        
//        String everything = sb.toString();
//        String[] as_paths = everything.split("\n");
//        
//        
//        
//        for(String path: as_paths){
//           Matcher m = r.matcher(path);
//           if(!m.find()){
//           List<Long> resultlist = tsk.PreprocessPath(path);
//           HashSet<Long> set = new LinkedHashSet<Long>(resultlist);
//           resultset_list.add(set);
//           }
//        }
        
        //LinkedHashSet<List<HashSet<Long>>> result_set = new LinkedHashSet(resultset_list);
       // System.out.println(result_set);
        
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        finally{
            filereader.close();
        }
        return ;
    }
    
}
