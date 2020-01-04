import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
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
                else if (att.equals("-2")) this.singleOrDoubleSpace(binariesArrayList, line);
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
            e.printStackTrace();
            //System.out.print("Error: something goes wrong. Cannot unhide message.\n");
        }
    }

    // additional space at the end of the line
    private int additionalSpaceAtEndOfLine(String line) {
        if (!line.equals("")) {
            if (line.substring(line.length() - 1).equals(" ")) return 1;
            else return 0;
        } else return 0;
    }

    // single or double space
    private void singleOrDoubleSpace(ArrayList<Integer> binariesArrayList, String line) {
        boolean open_tag = false;
        boolean close_tag = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.toCharArray()[i];
            if (c == '<') open_tag = true;
            if (c == '>') close_tag = true;

            // if I'm on the between < and > then I can read spaces
            if (open_tag) {
                if (c == ' ') {
                    i++;
                    c = line.toCharArray()[i];

                    // if next char is a space as well then I can read 1, otherwise I can read 0
                    if (c == ' ') binariesArrayList.add(1);
                    else binariesArrayList.add(0);
                }
            }

            if (close_tag) {
                open_tag = false;
                close_tag = false;
            }
        }
    }

    /*
    // typos in attribute names
    private String typosInAttributeNames(String line) {

    }

    // sequences closing and opening tags
    private String sequencesClosingAndOpeningTags(String line) {

    }

     */
}
