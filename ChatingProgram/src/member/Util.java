package member;

public class Util {
    
    public static int parseInt(String strNum, int defaultValue) {
        int value = defaultValue;
        
        try {
            value = Integer.parseInt(strNum);
        } catch(NumberFormatException e) {
            
        }
        return value;
    }
}
