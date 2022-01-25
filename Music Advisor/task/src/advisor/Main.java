package advisor;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean quit = false;
        boolean authOrExit;
        boolean started = true;
        while (!quit) {

            String choice = scanner.nextLine().toLowerCase();
            authOrExit = choice.equals("auth") || choice.equals("exit");

            while (started && !authOrExit) {
                System.out.println("Please, provide access for application.");
                choice = scanner.nextLine().toLowerCase();
                authOrExit = choice.equals("auth") || choice.equals("exit");
            }
            started = false;
            switch (choice) {
                case "auth":
                    System.out.println("https://accounts.spotify.com/authorize?client_id=fe5116329022495595d5b72fc1076a82&" +
                            "redirect_uri=http://localhost:8080&response_type=code");
                    System.out.println("---SUCCESS---");
                    break;
                case "new":
                    printNew();
                    break;
                case "featured":
                    printFeatured();
                    break;
                case "categories":
                    printCategories();
                    break;
                case "playlists mood":
                    printPlaylists();
                    break;
                case "exit":
                    quit = true;
                    System.out.println("---GOODBYE!---");
                    break;
                default:
                    System.out.println("No such choice.");
            }
        }
    }

    public static void printNew() {
        System.out.println("---NEW RELEASES---\n" +
                "Mountains [Sia, Diplo, Labrinth]\n" +
                "Runaway [Lil Peep]\n" +
                "The Greatest Show [Panic! At The Disco]\n" +
                "All Out Life [Slipknot]");

    }

    public static void printFeatured() {
        System.out.println("---FEATURED---\n" +
                "Mellow Morning\n" +
                "Wake Up and Smell the Coffee\n" +
                "Monday Motivation\n" +
                "Songs to Sing in the Shower");

    }

    public static void printCategories() {
        System.out.println("---CATEGORIES---" +
                "Top Lists\n" +
                "Pop\n" +
                "Mood\n" +
                "Latin");

    }

    public static void printPlaylists() {
        System.out.println("---MOOD PLAYLISTS---\n" +
                "Walk Like A Badass  \n" +
                "Rage Beats  \n" +
                "Arab Mood Booster  \n" +
                "Sunday Stroll");
    }
}
