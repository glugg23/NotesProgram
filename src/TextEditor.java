import java.util.ArrayList;
import java.util.Scanner;

public class TextEditor {
    private boolean writeMode = false;
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
    //TODO Strip all white space before commands
    public void use() {
        Scanner in = new Scanner(System.in);
        String command;

        do {
            if(this.promptMode) {
                System.out.print("*");
            }

            command = in.nextLine();

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

        this.writeMode = true;
        Scanner in = new Scanner(System.in);
        String line;

        while(this.writeMode) {
            line = in.nextLine();

            //End write mode if a single . is entered
            if(line.equals(".")) {
                this.writeMode = false;

            //Otherwise write line to buffer
            } else {
                this.buffer.add(line);
            }
        }
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