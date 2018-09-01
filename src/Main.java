/*
    NotesProgram - A simple notes management system
    Copyright (C) 2018 Max Leonhardt

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {
        try {
            FileHandler fileHandler = new FileHandler("log.txt");
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);

            //Log all messages
            //TODO Allow fine tuning of logging frequency
            LOGGER.setLevel(Level.ALL);

        } catch(IOException e) {
            e.printStackTrace();
        }

        //This fails when running in an IDE, so for now this will not end the program early
        if(System.console() == null) {
            LOGGER.log(Level.SEVERE, "This program needs to be run from the command line");
            //TODO Uncomment this when building final .jar
            //return;
        }

        LOGGER.log(Level.FINEST, "Starts running");

        SQL.initialSetup();

        Scanner in = new Scanner(System.in);
        int mainMenuChoice;

        do {
            System.out.println("NotesProgram Copyright (C) 2018 Max Leonhardt\n" +
                    "Menu\n" +
                    "\t1 - Enter note\n" +
                    "\t2 - Show all notes\n" +
                    "\t3 - Save note to file\n" +
                    "\t4 - Upload file\n" +
                    "\t5 - Encryption menu\n" +
                    "\t0 - Exit\n");

            System.out.print("-> ");

            try {
                mainMenuChoice = Integer.parseInt(in.nextLine());

            } catch(NumberFormatException e) {
                mainMenuChoice = -1;
            }

            switch(mainMenuChoice) {
                case 1:
                    TextEditor ed = new TextEditor();
                    LOGGER.log(Level.FINEST, "Entering text editor");
                    ed.use();
                    LOGGER.log(Level.FINEST, "Exiting text editor");
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
                                "\t2 - Encrypt and upload file\n" +
                                "\t3 - Decrypt and save note\n" +
                                "\t4 - Encrypt note\n" +
                                "\t5 - Decrypt note\n" +
                                "\t0 - Exit\n");

                        System.out.print("-> ");

                        try {
                            encryptionMenuChoice = Integer.parseInt(in.nextLine());

                        } catch(NumberFormatException e) {
                            encryptionMenuChoice = -1;
                        }

                        switch(encryptionMenuChoice) {
                            case 1:
                                Optional<byte[]> optionalKey = Encryption.generateKey();
                                optionalKey.ifPresent(Encryption::insertKey);
                                break;
                            case 2:
                                System.out.print("Enter file name: ");
                                String unencryptedFilename = in.nextLine();
                                Encryption.encryptFile(unencryptedFilename);
                                break;
                            case 3:
                                System.out.print("Enter note title: ");
                                String encryptedTitle = in.nextLine();

                                try {
                                    Encryption.decryptAndSaveNote(encryptedTitle);

                                } catch(IllegalArgumentException e) {
                                    LOGGER.log(Level.WARNING, e.toString(), e);
                                    e.printStackTrace();
                                }
                                break;
                            case 4:
                                System.out.print("Enter note title: ");
                                String noteTitle = in.nextLine();
                                Encryption.encryptNote(noteTitle);
                                break;
                            case 5:
                                System.out.print("Enter note title: ");
                                String encryptedNoteTitle = in.nextLine();

                                try {
                                    Encryption.decryptNote(encryptedNoteTitle);

                                } catch(IllegalArgumentException e) {
                                    LOGGER.log(Level.WARNING, e.toString(), e);
                                    e.printStackTrace();
                                }
                                break;
                            case 0:
                                break;
                            default:
                                System.out.println("Invalid option");
                                break;
                        }

                    } while(encryptionMenuChoice != 0);

                    break;
                case 0:
                    System.out.println("Goodbye");
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }

        } while(mainMenuChoice != 0);

        in.close();

        LOGGER.log(Level.FINEST, "Ends running");
        //TODO Add code here to delete log file if there are no warnings that are WARNING/SEVERE
    }
}