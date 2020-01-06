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
    private int message_bits_qty;
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

                switch (att) {
                    case "-1":
                        prepared_line = this.additionalSpaceAtEndOfLine(line);
                        break;

                    case "-2":
                        prepared_line = this.singleOrDoubleSpace(line);
                        break;

                    case "-3":
                        prepared_line = this.typosInAttributeNames(line);
                        break;

                    case "-4":
                        prepared_line = this.sequencesClosingAndOpeningTags(line);
                        break;
                }

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
            System.out.print("Error: something goes wrong. Cannot hide message. Check your cover.html file.\n");
        }
    }

    // additional space at the end of the line
    private String additionalSpaceAtEndOfLine(String line) {
        StringBuilder prepared_line = new StringBuilder();
        prepared_line.append(rtrim(line));

        // 0 - no space, 1 - space
        int bin = this.getBit(true);
        if (bin == 1) prepared_line.append(' ');
        return prepared_line.toString();
    }

    // single or double space
    // only withing '<', '>' tags
    private String singleOrDoubleSpace(String line) {
        boolean open_tag = false;
        boolean close_tag = false;

        // 1. format line based on original but with one space in '<', '>' tags
        // 2. do hide based od formatted line
        StringBuilder formatted_line = new StringBuilder();
        StringBuilder prepared_line = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '<') open_tag = true;
            else if (c == '>') close_tag = true;

            // if the char isn't within tags then add to prepared_line variable
            // if the char is after '<' char then add to formatted_line variable
            // if the char is equal to '>' char then add to formatted_line variable and remove all duplicate spaces
            // after that do hide the message
            if (!open_tag && !close_tag) prepared_line.append(c);
            else if (open_tag && !close_tag) formatted_line.append(c);
            else if (open_tag) {
                formatted_line.append(c);
                open_tag = false;
                close_tag = false;

                // remove duplicate spaces and hide the message
                String temp = formatted_line.toString().replaceAll(" +", " ");
                for (char c2 : temp.toCharArray()) {
                    prepared_line.append(c2);
                    if (c2 == ' ') {
                        int bin = this.getBit(true);

                        // if 0 the single space, if 1 then duplicate space
                        if (bin == 1) prepared_line.append(' ');
                    }
                }

                formatted_line = new StringBuilder();
            } else { // if close_tag is true
                prepared_line.append(c);
                close_tag = false;
            }
        }

        return prepared_line.toString();
    }

    // typos in attribute names
    private String typosInAttributeNames(String line) {
        StringBuilder prepared_line = new StringBuilder();
        String formatted_line = line
                .replaceAll("style=\"margin_botom:10px;\"", "")
                .replaceAll("style=\"pading-top:5px;\"", "");

        // if contains div then hide message in tag, otherwise take whole line and take next line
        if (formatted_line.contains("<div") || formatted_line.contains("<DIV")) {
            String[] splitted_line = splitBySpace(formatted_line);

            for (String s : splitted_line) {
                prepared_line.append(s);
                if (s.contains("<div") || s.contains("<DIV")) {
                    // 0 - margin-botom:10px, 1 - pading-top:5px
                    int bin = this.getBit(true);
                    if (bin == 0) prepared_line.append("style=\"margin_botom:10px;\" ");
                    else if (bin == 1) prepared_line.append("style=\"pading-top:5px;\" ");
                }
            }
        } else prepared_line.append(formatted_line);

        return prepared_line.toString();
    }

    // sequences closing and opening tags
    private String sequencesClosingAndOpeningTags(String line) {
        StringBuilder prepared_line = new StringBuilder();
        String formatted_line = line
                .replaceAll("<font>", "<FONT>")
                .replaceAll("</font>", "</FONT>")
                .replaceAll("<FONT></FONT></FONT>", "<FONT>")
                .replaceAll("</FONT><FONT></FONT>", "</FONT>");

        // if contains FONT then hide message as duplicate, otherwise take whole line and take next line
        if (formatted_line.contains("<FONT>") || formatted_line.contains("</FONT>")) {
            prepared_line = sequencesByTag(formatted_line);
        } else prepared_line.append(formatted_line);

        return prepared_line.toString()
                .replaceAll("<FONT_0>", "</FONT><FONT></FONT>")
                .replaceAll("<FONT_1>", "<FONT></FONT><FONT>");
    }

    private StringBuilder sequencesByTag(String line) {
        StringBuilder prepared_line = new StringBuilder();

        for (int i = 0; i <= line.length() - 7; i++) {
            String s = line.substring(i, i + 7);

            // 0 - </FONT><FONT></FONT>, 1 - <FONT></FONT><FONT>
            if (s.substring(0, 6).equals("<FONT>") && this.getBit(false) == 1) {
                this.nextBit();
                prepared_line.append("<FONT_1>");
                i += 5;
            } else if (s.equals("</FONT>") && this.getBit(false) == 0) {
                this.nextBit();
                prepared_line.append("<FONT_0>");
                i += 6;
            } else {
                prepared_line.append(line, i, i + 1);
            }
        }

        prepared_line.append(line, line.length() - 6, line.length());

        return prepared_line;
    }

    private int getBit(boolean next_bit) {
        if (this.hex_pos < this.message.size()) {
            String binaries = hexToBin(this.message.get(this.hex_pos));
            int bin = binaries.toCharArray()[this.bit_pos] - 48;
            if (next_bit) this.nextBit();

            return bin;
        }

        return 0;
    }

    private void nextBit() {
        this.bit_pos++;
        this.sequences_qty++;
        if (this.bit_pos > 7) {
            this.bit_pos = 0;
            this.hex_pos++;
        }
    }

    private String[] splitBySpace(String line) {
        String[] splitted_line = line.split(" ");
        for (int i = 0; i < splitted_line.length; i++) {
            if (i < splitted_line.length - 1) {
                StringBuilder builder = new StringBuilder();
                for (char c : splitted_line[i].toCharArray()) builder.append(c);
                builder.append(' ');
                splitted_line[i] = builder.toString();
            }
        }

        return splitted_line;
    }

    private static String rtrim(String s) {
        return RTRIM.matcher(s).replaceAll("");
    }
}
