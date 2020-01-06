import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Stegano {
    private static String path = System.getProperty("user.dir");

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2) printWrongArg();
            else {
                String cmd = args[0];
                String att = args[1];

                if (Integer.parseInt(att.substring(1)) < 1 || Integer.parseInt(att.substring(1)) > 4) printWrongArg();
                else {
                    switch (cmd) {
                        case "-e":  // hide message
                            if (prepareMessageFile()) { // prepare from plain file into hex version
                                ArrayList<String> message = readMessage();
                                if (message != null && message.size() > 0) {
                                    HideMessage hideMessage = new HideMessage(message, path);
                                    hideMessage.doHideMessage(att);
                                } else System.out.print("The message is empty.");
                            }

                            break;

                        case "-d":  // unhide message
                            UnhideMessage unhideMessage = new UnhideMessage(path);
                            unhideMessage.doUnhideMessage(att);
                            break;

                        default:
                            printWrongArg();
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String convertToHex(String line) {
        StringBuilder builder = new StringBuilder();
        for (char c : line.toCharArray()) builder.append(Integer.toHexString(c).toUpperCase());
        return builder.toString();
    }

    private static boolean prepareMessageFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/mess.txt"));
            Scanner scanner = new Scanner(new File(path + "/plain_mess.txt"));

            int row = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String prepared_line = convertToHex(line);

                System.out.printf("ROW %d:\n", row++);
                System.out.printf("> plain line:\n%s\n", line);
                System.out.printf("> prepared line:\n%s\n\n", prepared_line);

                writer.write(prepared_line);
                writer.write('\n');
            }

            writer.close();
            scanner.close();
            return true;
        } catch (Exception e) {
            System.out.print("Error: something goes wrong, check your plain_mess.txt file.\n");
            return false;
        }
    }

    private static ArrayList<String> readMessage() {
        Scanner scanner;

        try {
            scanner = new Scanner(new File(path + "/mess.txt"));
            ArrayList<String> arrayList = new ArrayList<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                char[] chars = line.toCharArray();
                for (int i = 0; i < chars.length; i += 2) arrayList.add(String.valueOf(chars[i]) + chars[i + 1]);
            }

            System.out.print("The message has been loaded successfully.\n");
            scanner.close();

            return arrayList;
        } catch (Exception e) {
            System.out.print("Error: message file not found\n");
        }

        return null;
    }

    private static void printWrongArg() {
        System.out.print("Unrecognized arguments, please type arg:\n" +
                "-e - hide message\n" +
                "-d - unhide message\n\n" +
                "with option:\n" +
                "-1 - additional space at the end of the line\n" +
                "-2 - single or double space\n" +
                "-3 - typos in attribute names\n" +
                "-4 - sequences closing and opening tags\n");
    }
}
