import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class UnhideMessage {
    private File watermarkFile;
    private String detectFilePath;
    private int line_qty = 0;
    private int sequences_qty = 0;
    ArrayList<Integer> binariesArrayList = new ArrayList<>();
    StringBuilder detectArr;

    public UnhideMessage(String path) {
        this.watermarkFile = new File(path + "/watermark.html");
        this.detectFilePath = path + "/detect.txt";
    }

    public void doUnhideMessage(String att) {
        try {
            Scanner scanner = new Scanner(watermarkFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                this.line_qty++;

                if (att.equals("-1")) this.additionalSpaceAtEndOfLine(line);
                else if (att.equals("-2")) this.singleOrDoubleSpace(line);
                else if (att.equals("-3")) this.typosInAttributeNames(line);
                //else if (att.equals("-4")) prepared_line = sequencesClosingAndOpeningTags(line);
            }

            scanner.close();

            StringBuilder detectMessage = new StringBuilder();
            StringBuilder builder = new StringBuilder();
            int bitPos = 0;

            for (int i : this.binariesArrayList) {
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
            writer.write(detectMessage.toString().trim());
            writer.close();

            System.out.printf("Unhiding done with attribute %s\n", att);
            System.out.printf("Rows: %d\nSequences: %d\n", this.line_qty, this.sequences_qty);
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.print("Error: something goes wrong. Cannot unhide message.\n");
        }
    }

    // additional space at the end of the line
    private void additionalSpaceAtEndOfLine(String line) {
        if (line.length() > 0) {
            if (line.substring(line.length() - 1).equals(" ")) this.addBinToArray(1);
            else this.addBinToArray(0);
        } else this.addBinToArray(0);
    }

    // single or double space
    private void singleOrDoubleSpace(String line) {
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
                    if (c == ' ') this.addBinToArray(1);
                    else this.addBinToArray(0);
                }
            }

            if (close_tag) {
                open_tag = false;
                close_tag = false;
            }
        }
    }

    // typos in attribute names
    private void typosInAttributeNames(String line) {
        String[] splittedLine = line.split(" ");
        for (String s : splittedLine) {
            // 0 - margin-botom: 10px, 1 - pading-top: 5px
            if (s.contains("style=\"margin_botom:10px;\"")) this.addBinToArray(0);
            else if (s.contains("style=\"pading-top:5px;\"")) this.addBinToArray(1);
        }
    }

    /*
    // sequences closing and opening tags
    private String sequencesClosingAndOpeningTags(String line) {

    }

     */

    private void addBinToArray(int bin) {
        this.binariesArrayList.add(bin);
        this.sequences_qty++;
        this.doDetect();
    }

    private void doDetect() {
        if (this.binariesArrayList.size() % 8 == 0) {
            detectArr = new StringBuilder();
            StringBuilder builder = new StringBuilder();
            int bitPos = 0;

            for (int i : this.binariesArrayList) {
                builder.append(i);
                bitPos++;

                if (bitPos == 8) {
                    bitPos = 0;
                    char c = (char) Integer.parseInt(builder.toString(), 2);
                    detectArr.append(c);
                    builder = new StringBuilder();
                }
            }
        }
    }
}
