import java.util.Arrays;
public class Echo {


    public  static  void printMsg(String msg){

        String msgArray[] = msg.split("[^a-zA-Z]+");

        String s = Arrays.stream(msgArray).filter(word -> !word.isEmpty()).toString();

        System.out.println(s);
    }
}
