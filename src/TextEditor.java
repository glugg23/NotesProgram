import java.util.ArrayList;
import java.util.Scanner;

public class TextEditor {
    private boolean writeMode = false;
    private int lineNumber = 0;
    private ArrayList<String> lines;

    public TextEditor() {
        this.lines = new ArrayList<>();
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

        String choice;

        do {
            //Check whether we are in write mode
            if(this.writeMode) {
                //Read in next line
                choice = in.nextLine();

                //If it's a dot stop being in write mode
                if(choice.equals(".")) {
                    this.writeMode = false;

                //Otherwise write line to ArrayList of lines
                } else {
                    this.lines.add(choice);
                    this.lineNumber++;
                }

            //If we're not in write mode
            } else {
                System.out.print("-> ");
                choice = in.nextLine();

                //Get new command
                switch(choice) {
                    case "i":
                        this.writeMode = true;
                        break;
                    case "w":
                        writeToNote(note);
                        break;
                    case "q":
                        break;
                    default:
                        System.out.println("?");
                        break;
                }
            }

        } while(!choice.equals("q"));

        return note;
    }

    /**
     * Write the whole buffer to one note, inserting a newline after each element in the array
     *
     * @param note The note where the buffer is written too
     */
    private void writeToNote(Note note) {
        for(String line : lines) {
            note.appendNote(line+"\n");
        }
    }
}