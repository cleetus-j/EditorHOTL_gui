import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractNumbersFromString {
    public static void main(String[] args) {
        String input = "abc123xyz456";
        
        // Define a regex pattern to match one or more digits
        Pattern pattern = Pattern.compile("\\d+");
        
        // Create a matcher for the input string
        Matcher matcher = pattern.matcher(input);
        
        // Find all matches and concatenate them
        StringBuilder numbers = new StringBuilder();
        while (matcher.find()) {
            numbers.append(matcher.group());
        }
        
        // Print the result
        System.out.println("Extracted numbers: " + numbers.toString());
    }
}
