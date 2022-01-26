package advisor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Main.SERVER_PATH = args.length != 0 && args[0].equals("-access") ?
                args[1] : "https://accounts.spotify.com";

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
                    createServer();
                    getToken();
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

    public static String SERVER_PATH;
    public static String REDIRECT_URI = "http://localhost:8080";
    public static String CLIENT_ID = "put";
    public static String CLIENT_SECRET = "put";
    public static String ACCESS_CODE = "";

    public static void createServer() {
        String uri = SERVER_PATH + "/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code";

        try {
            HttpServer server = HttpServer.create();

            server.bind(new InetSocketAddress(8080), 0);
            server.start();
            server.createContext("/",
                    new HttpHandler() {
                        public void handle(HttpExchange exchange) throws IOException {
                            String query = exchange.getRequestURI().getQuery();
                            String request;
                            if (query != null && query.contains("code")) {
                                ACCESS_CODE = query.substring(5);
                                System.out.println("code received");
                                System.out.println(ACCESS_CODE);
                                request = "Got the code. Return back to your program.";
                            } else {
                                request = "Authorization code not found. Try again.";
                            }
                            exchange.sendResponseHeaders(200, request.length());
                            exchange.getResponseBody().write(request.getBytes());
                            exchange.getResponseBody().close();
                        }
                    }
            );

            System.out.println("use this link to request the access code:");
            System.out.println(uri);
            System.out.println("waiting for code...");
            while (ACCESS_CODE.equals("")) {
                Thread.sleep(100);
            }
            server.stop(10);
        } catch (IOException | InterruptedException e) {
            System.out.println();

        }
        System.out.println();
    }

    public static void getToken() {
        System.out.println("making http request for access_token...");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(SERVER_PATH + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code"
                                + "&code=" + ACCESS_CODE
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET
                                + "&redirect_uri=" + REDIRECT_URI))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response:");
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
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
