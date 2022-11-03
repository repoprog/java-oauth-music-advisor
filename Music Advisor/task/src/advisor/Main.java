package advisor;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        List<String> commands = Arrays.asList(args);
        String SERVER_PATH = commands.contains("-access") ? args[1] : "https://accounts.spotify.com";
        MusicModel.setServerPath(SERVER_PATH);
        String API_PATH = commands.contains("-resource") ? args[3] : "https://api.spotify.com";
        MusicModel.setApiPath(API_PATH);
        String pages = commands.contains("-pages") ? args[5] : "5";
        MusicModel.setPages(Integer.parseInt(pages));


        MusicController.menu();

    }

}

