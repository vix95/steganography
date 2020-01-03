public class Stegano {
    private static String home_path = System.getProperty("user.dir");

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2) printWrongArg();
            else {
                String cmd = args[0];
                String att = args[1];

                switch (cmd) {
                    case "-e":  // hide message
                        switch (att) {
                            case "-1":  // additional space at the end of the line
                                // to do...
                                break;
                            case "-2":  // single or double space
                                // to do...
                                break;
                            case "-3":  // typos in attribute names
                                // to do...
                                break;
                            case "-4":  // sequences closing and opening tags
                                // to do..
                                break;
                            default:
                                printWrongArg();
                                break;
                        }

                        break;

                    case "-d":  // unhide message
                        switch (att) {
                            case "-1":  // additional space at the end of the line
                                // to do...
                                break;
                            case "-2":  // single or double space
                                // to do...
                                break;
                            case "-3":  // typos in attribute names
                                // to do...
                                break;
                            case "-4":  // sequences closing and opening tags
                                // to do..
                                break;
                            default:
                                printWrongArg();
                                break;
                        }
                        break;

                    default:
                        printWrongArg();
                        break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
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
