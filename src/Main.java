import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SQL.initialSetup();

        Scanner in = new Scanner(System.in);
        int mainMenuChoice;

        do {
            System.out.println("Notes Program V0.1\n" +
                    "Menu\n" +
                    "\t1 - Enter note\n" +
                    "\t2 - Show all notes\n" +
                    "\t3 - Save note to file\n" +
                    "\t4 - Upload file\n" +
                    "\t5 - Encryption menu\n" +
                    "\t0 - Exit\n");

            System.out.print("-> ");

            try {
                mainMenuChoice = in.nextInt();

            } catch(InputMismatchException e) {
                System.out.println("Invalid option");
                mainMenuChoice = -1;
            }

            switch(mainMenuChoice) {
                case 1:
                    TextEditor ed = new TextEditor();
                    ed.use();
                    break;
                case 2:
                    SQL.showAllNotes();
                    break;
                case 3:
                    System.out.print("Enter note title: ");
                    String title = in.nextLine();
                    SQL.saveNote(title);
                    break;
                case 4:
                    System.out.print("Enter file name: ");
                    String filename = in.nextLine();
                    SQL.uploadFile(filename);
                    break;
                case 5:
                    int encryptionMenuChoice;
                    do {
                        System.out.println("Encryption Menu\n" +
                                "\t1 - Generate key\n" +
                                "\t0 - Exit\n");

                        System.out.print("-> ");

                        try {
                            encryptionMenuChoice = in.nextInt();

                        } catch(InputMismatchException e) {
                            System.out.println("Invalid option");
                            encryptionMenuChoice = -1;
                        }

                        switch(encryptionMenuChoice) {
                            case 1:
                                try {
                                    byte[] key = Encryption.generateKey();
                                    Encryption.insertKey(key);

                                } catch(Exception e) {
                                    System.out.println(e.getMessage());
                                }
                                break;
                            case 0:
                                break;
                            default:
                                in.nextLine();
                                break;
                        }

                    } while(encryptionMenuChoice != 0);

                    break;
                case 0:
                    System.out.println("Goodbye");
                    break;
                default:
                    in.nextLine();
                    break;
            }

        } while(mainMenuChoice != 0);

        in.close();
    }
}