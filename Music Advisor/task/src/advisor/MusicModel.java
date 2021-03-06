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

public class MusicModel {
    private static String SERVER_PATH;
    private static String REDIRECT_URI = "http://localhost:8080";
    private static String CLIENT_ID = "fe5116329022495595d5b72fc1076a82";
    private static String CLIENT_SECRET = "2b27ffc709364928bd6e3a8e802224d0";
    private static String ACCESS_CODE = "";
    private static String accessToken;
    private static String API_PATH;

    public static String getServerPath() {
        return SERVER_PATH;
    }

    public static void setServerPath(String serverPath) {
        SERVER_PATH = serverPath;
    }

    public static String getApiPath() {
        return API_PATH;
    }

    public static void setApiPath(String apiPath) {
        API_PATH = apiPath;
    }

    public static String getAccessToken() {
        return accessToken;
    }

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
}
