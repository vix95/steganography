import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HideMessage {
    private ArrayList<String> message;
    private String watermarkFilePath;
    private File coverFile;
    private int bit_pos = 0;
    private int hex_pos = 0;
    private int line_qty = 0;
    private int sequences_qty = 0;
    private int message_bits_qty = 0;
    private final static Pattern RTRIM = Pattern.compile("\\s+$");

    public HideMessage(ArrayList<String> message, String path) {
        this.message = message;
        this.watermarkFilePath = path + "/watermark.html";
        this.coverFile = new File(path + "/cover.html");
        this.message_bits_qty = message.size() * 8;
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
                this.line_qty++;

                if (att.equals("-1")) prepared_line = this.additionalSpaceAtEndOfLine(line);
                else if (att.equals("-2")) prepared_line = this.singleOrDoubleSpace(line);
                else if (att.equals("-3")) prepared_line = this.typosInAttributeNames(line);
                //else if (att.equals("-4")) prepared_line = sequencesClosingAndOpeningTags(line);

                if (prepared_line != null) {
                    writer.write(prepared_line);
                    writer.write('\n');
                }
            }

            writer.close();
            scanner.close();

            if (this.sequences_qty < this.message_bits_qty) {
                System.out.printf("WARNING: Hiding done with attribute %s but not hidden whole message.\n", att);
                System.out.print("You should take another cover.html file or type shorter message.\n");
                System.out.printf("Hidden %d/%d bits.\n", this.sequences_qty, this.message_bits_qty);
            } else {
                System.out.printf("Hiding done with attribute %s.\n", att);
                System.out.printf("Rows: %d\nSequences: %d\n", this.line_qty, this.sequences_qty);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.print("Error: something goes wrong. Cannot hide message.\n");
        }
    }

    // additional space at the end of the line
    private String additionalSpaceAtEndOfLine(String line) {
        StringBuilder preparedLine = new StringBuilder();
        preparedLine.append(rtrim(line));

        // 0 - no space, 1 - space
        int bin = this.getBin();
        if (bin == 1) preparedLine.append(' ');
        return preparedLine.toString();
    }

    // single or double space
    // only withing '<', '>' tags
    private String singleOrDoubleSpace(String line) {
        boolean open_tag = false;
        boolean close_tag = false;

        // 1. format line based on original but with one space in '<', '>' tags
        // 2. do hide based od formatted line
        StringBuilder formattedLine = new StringBuilder();
        StringBuilder preparedLine = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '<') open_tag = true;
            else if (c == '>') close_tag = true;

            // if the char isn't within tags then add to preparedLine variable
            // if the char is after '<' char then add to formattedLine variable
            // if the char is equal to '>' char then add to formattedLine variable and remove all duplicate spaces
            // after that do hide the message
            if (!open_tag && !close_tag) preparedLine.append(c);
            else if (open_tag && !close_tag) formattedLine.append(c);
            else if (open_tag) {
                formattedLine.append(c);
                open_tag = false;
                close_tag = false;

                // remove duplicate spaces and hide the message
                String temp = formattedLine.toString().replaceAll(" +", " ");
                for (char c2 : temp.toCharArray()) {
                    preparedLine.append(c2);
                    if (c2 == ' ') {
                        int bin = this.getBin();

                        // if 0 the single space, if 1 then duplicate space
                        if (bin == 1) preparedLine.append(' ');
                    }
                }
            }
        }

        return preparedLine.toString();
    }

    // typos in attribute names
    private String typosInAttributeNames(String line) {
        StringBuilder preparedLine = new StringBuilder();

        // if contains div then hide message in tag, otherwise take whole line and take next line
        if (line.contains("<div")) {
            String[] splittedLine = splitBySpace(line);

            for (String s : splittedLine) {
                preparedLine.append(s);
                if (s.contains("<div")) {
                    int bin = this.getBin();
                    // 0 - margin-botom:10px, 1 - pading-top:5px
                    if (bin == 0) preparedLine.append("style=\"margin_botom:10px;\" ");
                    else if (bin == 1) preparedLine.append("style=\"pading-top:5px;\" ");
                }
            }
        } else preparedLine.append(line);

        return preparedLine.toString();
    }

    /*
    // sequences closing and opening tags
    private String sequencesClosingAndOpeningTags(String line) {

    }

     */

    private int getBin() {
        if (this.hex_pos < this.message.size()) {
            String binaries = hexToBin(this.message.get(this.hex_pos));
            int bin = binaries.toCharArray()[this.bit_pos] - 48;
            this.nextBit();
            this.sequences_qty++;
            return bin;
        }

        return 0;
    }

    private void nextBit() {
        this.bit_pos++;
        if (this.bit_pos > 7) {
            this.bit_pos = 0;
            this.hex_pos++;
        }
    }

    private String[] splitBySpace(String line) {
        String[] splittedLine = line.split(" ");
        for (int i = 0; i < splittedLine.length; i++) {
            if (i < splittedLine.length - 1) {
                StringBuilder builder = new StringBuilder();
                for (char c : splittedLine[i].toCharArray()) builder.append(c);
                builder.append(' ');
                splittedLine[i] = builder.toString();
            }
        }

        return splittedLine;
    }

    private static String rtrim(String s) {
        return RTRIM.matcher(s).replaceAll("");
    }
}
