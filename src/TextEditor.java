import java.util.ArrayList;
import java.util.Scanner;

public class TextEditor {
    private boolean writeMode = false;
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
        //Note note = new Note(title);

        String command;

        do {
            System.out.print("-> ");
            command = in.nextLine();

            //Get first char for command
            switch(command.charAt(0)) {
                case 'i':
                    i(command);
                    break;
                case 'w':
                    w(command);
                    break;
                case 'q':
                    break;
                default:
                    this.errorMessage = "Unknown command";
                    System.out.println("?");
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
        if(command.length() > 1) {
            this.errorMessage = "Unknown command";
            System.out.println("?");
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
        try {
            if(command.charAt(1) != ' ') {
                this.errorMessage = "No filename input";
                System.out.println("?");
                return;
            }
        //Case for when only w is inputted
        } catch(StringIndexOutOfBoundsException e) {
            this.errorMessage = "No filename input";
            System.out.println("?");
            return;
        }

        this.filename = command.substring(2);
        Note note = new Note(this.filename);

        for(String line : this.buffer) {
            note.appendNote(line + "\n");
        }

        note.upload();
    }
}