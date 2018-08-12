import java.util.ArrayList;
import java.util.Scanner;

public class TextEditor {
    private boolean writeMode = false;
    private ArrayList<String> buffer;
    private String errorMessage;

    public TextEditor() {
        this.buffer = new ArrayList<>();
    }

    /**
     * Runs a text editor similar to ed, allowing the user to write more complex notes
     *
     * parameters may change to not take in a title
     *
     * @param title The title for the note
     * @return A note object with the title and content inputted
     */
    public Note use(String title) {
        Scanner in = new Scanner(System.in);
        Note note = new Note(title);

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
                    writeToNote(note);
                    break;
                case 'q':
                    break;
                default:
                    this.errorMessage = "Unknown command";
                    System.out.println("?");
                    break;
            }

        } while(!command.equals("q"));

        return note;
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
     * Write the whole buffer to one note, inserting a newline after each element in the array
     *
     * @param note The note where the buffer is written too
     */
    private void writeToNote(Note note) {
        for(String line : buffer) {
            note.appendNote(line+"\n");
        }
    }
}