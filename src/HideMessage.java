import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

public class HideMessage {
    private ArrayList<String> message;
    private String watermarkFilePath;
    private File coverFile;
    private int bitPos = 0;
    private int hexPos = 0;

    public HideMessage(ArrayList<String> message, String path) {
        this.message = message;
        this.watermarkFilePath = path + "/watermark.html";
        this.coverFile = new File(path + "/cover.html");
    }

    static String hexToBin(String s) {
        return String.format("%8s", new BigInteger(s, 16).toString(2)).replace(' ', '0');
    }

    public void doHideMessage(String att) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(watermarkFilePath));
            Scanner scanner = new Scanner(coverFile);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String prepared_line = null;

                if (att.equals("-1")) prepared_line = this.additionalSpaceAtEndOfLine(line);
                else if (att.equals("-2")) prepared_line = this.singleOrDoubleSpace(line);
                //else if (att.equals("-3")) prepared_line = typosInAttributeNames(line);
                //else if (att.equals("-4")) prepared_line = sequencesClosingAndOpeningTags(line);

                if (prepared_line != null) {
                    writer.write(prepared_line);
                    writer.write('\n');
                }
            }

            writer.close();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.print("Error: something goes wrong. Cannot hide message.\n");
        }
    }

    // additional space at the end of the line
    private String additionalSpaceAtEndOfLine(String line) {
        StringBuilder preparedLine = new StringBuilder();
        preparedLine.append(line);

        // 0 - no space, 1 - space
        int bin = this.getBin();
        if (bin == 1) preparedLine.append(' ');
        return preparedLine.toString();
    }

    // single or double space
    private String singleOrDoubleSpace(String line) {
        StringBuilder preparedLine = new StringBuilder();
        boolean open_tag = false;
        boolean close_tag = false;

        for (char c : line.toCharArray()) {
            if (c == '<') open_tag = true;
            if (c == '>') close_tag = true;

            // if I'm on the between < and > then I can modify spaces
            if (open_tag) {
                if (c == ' ') {
                    int bin = this.getBin();

                    // if 0 the single space, if 1 then duplicate space
                    if (bin == 1) preparedLine.append(' ');
                }
            }

            if (close_tag) {
                open_tag = false;
                close_tag = false;
            }

            preparedLine.append(c);
        }

        return preparedLine.toString();
    }

    /*
    // typos in attribute names
    private String typosInAttributeNames(String line) {

    }

    // sequences closing and opening tags
    private String sequencesClosingAndOpeningTags(String line) {

    }

     */

    private int getBin() {
        if (this.hexPos < this.message.size()) {
            String binaries = hexToBin(this.message.get(this.hexPos));
            int bin = binaries.toCharArray()[this.bitPos] - 48;
            this.nextBit();
            return bin;
        }

        return 0;
    }

    private void nextBit() {
        this.bitPos++;
        if (this.bitPos > 7) {
            this.bitPos = 0;
            this.hexPos++;
        }
    }
}
