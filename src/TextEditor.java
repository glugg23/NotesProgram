import java.util.ArrayList;
import java.util.Scanner;

public class TextEditor {
    private boolean promptMode = false;
    private boolean helpMode = false;
    private ArrayList<String> buffer;
    private String errorMessage;
    private String filename = "";

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

            //Find addresses for command
            for(int i = 0; i < command.length(); ++i) {
                if(Character.isDigit(command.charAt(i))) {
                    //Handle setting addresses

                } else {
                    switch(command.charAt(i)) {
                        //Otherwise check for special cases
                        case '.': //Current line in buffer
                        case '$': //Last line in buffer
                        case ',': //First through last lines in buffer TODO Make sure this doesn't work strange when in between 2 addresses
                        case ';': //Current through last lines in buffer
                        case '+': //Next line TODO Make sure this works with multiple +'s in a row
                        case '-': //Previous line TODO See last todo
                        //TODO Add cases not handled: regex on this line, and line previous, and +- followed by number
                        default:
                            /*
                            Fails when:
                            No address given
                            Any address is negative
                            Second address is larger than buffer size

                            Special case:
                            If there is only one address given, second address should be set equal to first
                             */
                            break;
                    }
                }
            }

            if(!command.isEmpty()) {
                while(Character.isWhitespace(command.charAt(0))) {
                    command = command.substring(1);
                }

            } else {
                //This should be null command, where if an address is found before that, it should print that address
                this.errorMessage = "Unknown command";
                System.out.println("?");
                if(this.helpMode) {
                    System.out.println(this.errorMessage);
                }
                //This command has already been handled so we don't want to go to the switch statement
                continue;
            }

            //Get first char for command
            switch(command.charAt(0)) {
                case 'i':
                    i(command);
                    break;
                case 'w':
                    w(command);
                    break;
                case 'P':
                    if(!command.equals("P")) {
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
                    if(!command.equals("h")) {
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
                    if(!command.equals("H")) {
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
}