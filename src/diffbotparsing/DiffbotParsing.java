/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diffbotparsing;
import java.net.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/**Class that uses the Diffbot Article API to capture the semantic tags recognized for a set of links
 * You have to define your Diffbot API, the links you would like to analyze and a directory to save the txt file with the tags
 * It uses JSON-simple(code.google.com/p/json-simple) for the JSON parsing 
 * @author Themis Mavridis
 */
public class DiffbotParsing {
    public static HttpURLConnection httpCon;
    public static String connect(URL link_ur) {
        try{
            String line="";
            httpCon = (HttpURLConnection) link_ur.openConnection();
            if (httpCon.getResponseCode() != 200) {
                line = "fail";
                return line;
                // throw new IOException(httpCon.getResponseMessage());
            }
            else{
                BufferedReader rd = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();
                rd.close();
                String output=DiffbotJSONParsing(line);
                return output;
            }
        }
        catch (IOException ex) {
            Logger.getLogger(DiffbotParsing.class.getName()).log(Level.SEVERE, null, ex);
            String line="fail";
            return line;
        }
    }
    public static void main(String[] args){
        int links_size=1;
        String[] links=new String[links_size];
        links[0]="http://thesmartweb.eu";
        String directory="C:\\themis\\DiffbotDirectory\\";
        String token="<your token here>";
        if(args!=null&&args.length==8){
            for(int i=0;i<links_size;i++){
                links[i]=args[i+1];
            }
            if(args[links_size+1]!=null){
                directory=args[links_size+1];
            }
            if(args[links_size+1]!=null){
                token=args[links_size+2];
            }
        }
        compute(links,directory,token);
    }
    public static List<String> compute (String[] links,String directory,String token){
        List<String> wordList=null;
        try{
            URL diff_url = null;
            String stringtosplit="";
            for(int i=0;i<links.length;i++){
                if(!(links[i]==null)){
                    diff_url = new URL("http://api.diffbot.com/v2/article?token="+token+"&fields=tags,meta&url="+links[i]);
                    stringtosplit=connect(diff_url);
                      if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){       
                        stringtosplit=stringtosplit.replaceAll("[\\W&&[^\\s]]", "");
                        if(!(stringtosplit==null)&&(!(stringtosplit.equalsIgnoreCase("")))){
                            String[] tokenizedTerms=stringtosplit.split("\\W+");    //to get individual terms
                             for(int j=0;j<tokenizedTerms.length;j++){
                                if(!(tokenizedTerms[j]==null)&&(!(tokenizedTerms[j].equalsIgnoreCase("")))){
                                    wordList.add(tokenizedTerms[j]);
                                }    
                            }
                        }
                    }
                }
            }
            File file_words = new File(directory + "words.txt");
            FileUtils.writeLines(file_words,wordList);
            return wordList;
        }
        catch (MalformedURLException ex) {
            Logger.getLogger(DiffbotParsing.class.getName()).log(Level.SEVERE, null, ex);
            return wordList;
        } catch (IOException ex) {
            Logger.getLogger(DiffbotParsing.class.getName()).log(Level.SEVERE, null, ex);
            return wordList;
        }
    }
    public static String DiffbotJSONParsing(String input){
        String output=""; 
        try {
            //Create a parser
            JSONParser parser = new JSONParser();
            //Create the map
            Map json = (Map) parser.parse(input);
            // Get a set of the entries
            Set set = json.entrySet();
            Object[] arr = set.toArray();
            Map.Entry entry = (Map.Entry) arr[0];
            //****get to second level of  jsonmap to get the tags
            Object value = entry.getValue();
            String you = entry.getValue().toString();
            output=removeChars(you).toLowerCase();
            return output;
        }
          catch (ParseException ex) {
            Logger.getLogger(DiffbotParsing.class.getName()).log(Level.SEVERE, null, ex);
            output="fail";
             return output;
        }
    }
     public static String removeChars(String str){
        if (str != null) {
            try {
                //str = str.replaceAll("(\r\n|\r|\n|\n\r)", " "); //Clear Paragraph escape sequences
                str = str.replaceAll("\\.", " "); //Clear dots
                str = str.replaceAll("\\-", " "); //
                str = str.replaceAll("\\_", " "); //
                str = str.replaceAll(":", " ");
                str = str.replaceAll("\\+", " ");
                str = str.replaceAll("\\/", " ");
                str = str.replaceAll("\\|", " ");
                str = str.replaceAll("\\[", " ");
                str = str.replaceAll("\\?", " ");
                str = str.replaceAll("\\#", " ");
                str = str.replaceAll("\\!", " ");
                str = str.replaceAll("'", " "); //Clear apostrophes
                str = str.replaceAll(",", " "); //Clear commas
                str = str.replaceAll("@", " "); //Clear @'s (optional)
                str = str.replaceAll("$", " "); //Clear $'s (optional)
                str = str.replaceAll("\\\\", "**&**"); //Clear special character backslash 4 \'s due to regexp format
                str = str.replaceAll("&amp;", "&"); //change &amp to &
                str = str.replaceAll("&lt;", "<"); //change &lt; to <
                str = str.replaceAll("&gt;", ">"); //change &gt; to >
                //		str = str.replaceAll("<[^<>]*>"," ");		//drop anything in <>
                str = str.replaceAll("&#\\d+;", " "); //change &#[digits]; to space
                str = str.replaceAll("&quot;", " "); //change &quot; to space
                //		str = str.replaceAll("http://[^ ]+ "," ");	//drop urls
                str = str.replaceAll("-", " "); //drop non-alphanumeric characters
                str = str.replaceAll("[^0-9a-zA-Z ]", " "); //drop non-alphanumeric characters
                str = str.replaceAll("&middot;", " ");
                str = str.replaceAll("\\>", " ");
                str = str.replaceAll("\\<", " ");
                str = str.replaceAll("<[^>]*>", "");
                str = str.replaceAll("\\d"," ");
                //str=str.replaceAll("\\<.*?\\>", "");
                str = str.replace('β', ' ');
                str = str.replace('€', ' ');
                str = str.replace('™', ' ');
                str = str.replace(')', ' ');
                str = str.replace('(', ' ');
                str = str.replace('[', ' ');
                str = str.replace(']', ' ');
                str = str.replace('`', ' ');
                str = str.replace('~', ' ');
                str = str.replace('!', ' ');
                str = str.replace('#', ' ');
                str = str.replace('%', ' ');
                str = str.replace('^', ' ');
                str = str.replace('*', ' ');
                str = str.replace('&', ' ');
                str = str.replace('_', ' ');
                str = str.replace('=', ' ');
                str = str.replace('+', ' ');
                str = str.replace('|', ' ');
                str = str.replace('\\', ' ');
                str = str.replace('{', ' ');
                str = str.replace('}', ' ');
                str = str.replace(',', ' ');
                str = str.replace('.', ' ');
                str = str.replace('/', ' ');
                str = str.replace('?', ' ');
                str = str.replace('"', ' ');
                str = str.replace(':', ' ');
                str = str.replace('>', ' ');
                str = str.replace(';', ' ');
                str = str.replace('<', ' ');
                str = str.replace('$', ' ');
                str = str.replace('-', ' ');
                str = str.replace('@', ' ');
                str = str.replace('©', ' ');
                //remove space
                InputStreamReader in = new InputStreamReader(IOUtils.toInputStream(str));
                BufferedReader br = new BufferedReader(in);
                Pattern p;
                Matcher m;
                String afterReplace = "";
                String strLine;
                String inputText = "";
                while ((strLine = br.readLine()) != null) {
                    inputText = strLine;
                    p = Pattern.compile("\\s+");
                    m = p.matcher(inputText);
                    afterReplace = afterReplace + m.replaceAll(" ");
                }
                br.close();
                str = afterReplace;
                return str;
            } catch (IOException ex) {
                Logger.getLogger(DiffbotParsing.class.getName()).log(Level.SEVERE, null, ex);
                str=null;
                return str;
            }
        } else {
            return str;
        }
    }
 }
