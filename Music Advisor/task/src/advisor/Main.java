package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> commands = Arrays.asList(args);
        Main.SERVER_PATH = commands.contains("-access") ?
                args[1] : "https://accounts.spotify.com";
        Main.API_PATH = commands.contains("-resource") ?
                args[3] : "https://api.spotify.com";

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
                    System.out.println("Success!");
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
    public static String accessToken;
    public static String API_PATH;

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
        System.out.println("Making http request for access_token...");
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
            String json = response.body();
            JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
            accessToken = jo.get("access_token").getAsString();
            System.out.println(response.body());

        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void printNew() {
        String apiUrl = API_PATH +  "/v1/browse/new-releases";
        System.out.println(makeGetRequest(apiUrl));
    }


    public static void printFeatured() {
        String apiUrl = API_PATH +  "/v1/browse/featured-playlists";
        System.out.println(makeGetRequest(apiUrl));
    }

    public static void printCategories() {
        String apiUrl = API_PATH +  "/v1/browse/categories";
        System.out.println(makeGetRequest(apiUrl));
    }

    public static void printPlaylists() {
        String apiUrl = API_PATH +  "/v1/browse/categories/{category_id}/playlists";
        System.out.println(makeGetRequest(apiUrl));
    }

    public static String makeGetRequest(String apiUrl) {
        String json = null;
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            json = response.body();
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return json;
    }
}

