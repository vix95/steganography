import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class UnhideMessage {
    private File watermarkFile;
    private String detectFilePath;

    public UnhideMessage(String path) {
        this.watermarkFile = new File(path + "/watermark.html");
        this.detectFilePath = path + "/detect.txt";
    }

    public void doUnhideMessage(String att) {
        try {
            Scanner scanner = new Scanner(watermarkFile);
            ArrayList<Integer> binariesArrayList = new ArrayList<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (att.equals("-1")) binariesArrayList.add(this.additionalSpaceAtEndOfLine(line));
                //else if (att.equals("-2")) prepared_line = singleOrDoubleSpace(line);
                //else if (att.equals("-3")) prepared_line = typosInAttributeNames(line);
                //else if (att.equals("-4")) prepared_line = sequencesClosingAndOpeningTags(line);
            }

            scanner.close();

            StringBuilder detectMessage = new StringBuilder();
            StringBuilder builder = new StringBuilder();
            int bitPos = 0;
            for (int i : binariesArrayList) {
                builder.append(i);
                bitPos++;

                if (bitPos == 8) {
                    bitPos = 0;
                    char c = (char) Integer.parseInt(builder.toString(), 2);
                    detectMessage.append(c);
                    builder = new StringBuilder();
                }
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(detectFilePath));
            writer.write(detectMessage.toString());
            writer.close();
        } catch (Exception e) {
            System.out.print("Error: something goes wrong. Cannot unhide message.\n");
        }
    }

    // additional space at the end of the line
    private int additionalSpaceAtEndOfLine(String line) {
        if (!line.equals("")) {
            if (line.substring(line.length() - 1).equals(" ")) return 1;
            else return 0;
        } else return 0;
    }

    /*
    // single or double space
    private String singleOrDoubleSpace(String line) {

    }

    // typos in attribute names
    private String typosInAttributeNames(String line) {

    }

    // sequences closing and opening tags
    private String sequencesClosingAndOpeningTags(String line) {

    }

     */
}
