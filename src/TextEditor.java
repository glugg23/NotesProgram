import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor {
    private boolean promptMode = false;
    private boolean helpMode = false;
    private ArrayList<String> buffer;
    private String errorMessage;
    private String filename = "";
    private int firstAddress = 0;
    private int secondAddress = 0;

    public TextEditor() {
        this.buffer = new ArrayList<>();
    }

    /**
     * Runs a text editor similar to ed
     */
    public void use() {
        Scanner in = new Scanner(System.in);
        String command;

        do {
            if(this.promptMode) {
                System.out.print("*");
            }

            command = in.nextLine();

            //Removes all whitespace
            if(!command.isEmpty()) {
                command = command.replaceAll("\\s", "");

            } else {
                //TODO This is null command which prints next line after current position
                this.errorMessage = "Unknown command";
                System.out.println("?");
                if(this.helpMode) {
                    System.out.println(this.errorMessage);
                }
                //This command has already been handled so we don't want to go to the switch statement
                continue;
            }

            int commandPosition = extractAddresses(command);

            if((this.firstAddress > this.secondAddress) || (this.secondAddress > this.buffer.size())) {
                this.errorMessage = "Invalid address";
                System.out.println("?");
                if(this.helpMode) {
                    System.out.println(this.errorMessage);
                }
                continue;
            }

            //Get first char for command
            switch(command.charAt(commandPosition)) {
                case 'i':
                    i(command.substring(commandPosition));
                    break;
                case 'w':
                    w(command.substring(commandPosition));
                    break;
                case 'p':
                    p(command.substring(commandPosition));
                    break;
                case 'P':
                    if(!command.substring(commandPosition).equals("P")) {
                        this.errorMessage = "Unknown command";
                        System.out.println("?");
                        if(this.helpMode) {
                            System.out.println(this.errorMessage);
                        }

                    } else {
                        this.promptMode = !this.promptMode;
                    }
                    break;
                case 'h':
                    if(!command.substring(commandPosition).equals("h")) {
                        this.errorMessage = "Unknown command";
                        System.out.println("?");
                        if(this.helpMode) {
                            System.out.println(this.errorMessage);
                        }

                    } else {
                        System.out.println(this.errorMessage);
                    }
                    break;
                case 'H':
                    if(!command.substring(commandPosition).equals("H")) {
                        this.errorMessage = "Unknown command";
                        System.out.println("?");
                        if(this.helpMode) {
                            System.out.println(this.errorMessage);
                        }

                    } else {
                        this.helpMode = !this.helpMode;
                    }
                    break;
                case 'q':
                    break;
                default:
                    this.errorMessage = "Unknown command";
                    System.out.println("?");
                    if(this.helpMode) {
                        System.out.println(this.errorMessage);
                    }
                    break;
            }

        } while(!command.equals("q"));
    }

    /**
     * Using a regular expression to extract decimal numbers from a string
     *
     * @param command The command to extract an address from
     * @return A decimal string or an empty string if nothing was found
     */
    private String extractNumber(String command) {
        Pattern regex = Pattern.compile("\\d+");
        Matcher matcher = regex.matcher(command);

        //Finds the first match
        if(matcher.find()) {
            return matcher.group();

        } else {
            return "";
        }
    }

    /**
     * Takes in a full command and finds 1 or more addresses, and returns a position in the String
     * where the actually command is
     *
     * @param command The command where the addresses need to be extracted from
     * @return The location of the command to be executed
     */
    private int extractAddresses(String command) {
        //Clear address variables
        this.firstAddress = this.secondAddress = 0;

        boolean firstAddressFound = false;

        //Find addresses for command
        for(int i = 0; i < command.length(); ++i) {
            if(Character.isDigit(command.charAt(i))) {
                String number = extractNumber(command.substring(i));

                if(!firstAddressFound) {
                    firstAddressFound = true;

                    this.firstAddress = Integer.parseInt(number);
                    this.secondAddress = Integer.parseInt(number);

                } else {
                    this.secondAddress = Integer.parseInt(number);
                }

                i += number.length() - 1;

            } else {
                switch(command.charAt(i)) {
                    //Otherwise check for special cases
                    case '.': //Current line in buffer
                    case '$': //Last line in buffer
                    case ',': //First through last lines in buffer

                        //TODO Make this work more like the actual ed source code

                        break;
                    case ';': //Current through last lines in buffer
                    case '+': //Next line TODO Make sure this works with multiple +'s in a row
                    case '-': //Previous line TODO See last todo
                        //TODO Add cases not handled: regex on this line, and line previous, and +- followed by number
                    default:
                        //Returns position of where the command is
                        return i;
                }
            }
        }

        //TODO Maybe change this, but the exception can be useful to find out when somethig breaks
        //Returns -1 if this does not happen
        return -1;
    }

    /**
     * Takes in input from the user and writes it to the buffer
     *
     * @param command The rest of the command for error checking
     */
    private void i(String command) {
        //Check to make sure there isn't anymore to the command
        if(!command.equals("i")) {
            this.errorMessage = "Unknown command";
            System.out.println("?");
            if(this.helpMode) {
                System.out.println(this.errorMessage);
            }
            return;
        }

        Scanner in = new Scanner(System.in);
        String line;

        do {
            line = in.nextLine();

            if(!line.equals(".")) {
                this.buffer.add(line);
            }

        } while(!line.equals("."));
    }

    /**
     * Writes current buffer to note, using rest of command for filename/title
     *
     * @param command The rest of the command for error checking and to get the filename
     */
    private void w(String command) {
        //If only w is inputted handle case for when a filename already exists and when it does not
        if(command.equals("w")) {
            if(this.filename.isEmpty()) {
                this.errorMessage = "No filename input";
                System.out.println("?");
                if(this.helpMode) {
                    System.out.println(this.errorMessage);
                }

            } else {
                Note note = new Note(this.filename);

                for(String line : this.buffer) {
                    note.appendNote(line + "\n");
                }

                note.update();
            }

        //Otherwise check the command and see if it is valid
        } else if(command.length() < 3 || command.charAt(1) != ' ') {
            this.errorMessage = "Incorrect command format";
            System.out.println("?");
            if(this.helpMode) {
                System.out.println(this.errorMessage);
            }

        } else {
            this.filename = command.substring(2);
            Note note = new Note(this.filename);

            for(String line : this.buffer) {
                note.appendNote(line + "\n");
            }

            note.upload();
        }
    }

    private void p(String command) {
        //Check to make sure just "p" is being passed
        if(!command.equals("p")) {
            this.errorMessage = "Unknown command";
            System.out.println("?");
            if(this.helpMode) {
                System.out.println(this.errorMessage);
            }
            return;
        }

        for(int i = this.firstAddress; i <= this.secondAddress; ++i) {
            System.out.println(this.buffer.get(i-1));
        }
    }
}