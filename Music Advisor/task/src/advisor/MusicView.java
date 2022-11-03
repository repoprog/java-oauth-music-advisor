package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

public class MusicView {
    public static void printNew(String json) {
        List<JsonObject> albumItems = new ArrayList<>();

        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject albumsObj = jo.getAsJsonObject("albums");

        for (JsonElement album : albumsObj.getAsJsonArray("items")) {
            albumItems.add(album.getAsJsonObject());
        }

        for (JsonObject album : albumItems) {
            System.out.println(album.get("name").getAsString());
            List<String> artistList = new ArrayList<>();
            for (JsonElement el : album.getAsJsonArray("artists")) {
                //take object from JSON Array "artists" and print "name" and "spotify" elements from it
                artistList.add(el.getAsJsonObject().get("name").getAsString());
            }
            System.out.println(artistList);
            System.out.println(album.getAsJsonObject().get("external_urls")
                    .getAsJsonObject().get("spotify").getAsString() + "\n");
        }
    }

    public static void printFeatured(String json) {
        List<JsonObject> featuredItems = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject featuredObj = jo.getAsJsonObject("playlists");
        for (JsonElement featured : featuredObj.getAsJsonArray("items")) {
            featuredItems.add(featured.getAsJsonObject());
        }

        for (JsonObject featured : featuredItems) {
            System.out.println(featured.get("name").getAsString());
            System.out.println(featured.getAsJsonObject().get("external_urls")
                    .getAsJsonObject().get("spotify").getAsString() + "\n");
        }
    }

    public static void printCategories(Map<String, String> categoriesMap) {
        categoriesMap.forEach((k, v) -> System.out.println(k + "  " + v));
    }

    public static void printPlaylists(String json) {

        if (json.contains("error")) {
            String errorMsg = JsonParser.parseString(json).getAsJsonObject()
                    .getAsJsonObject("error").get("message").getAsString();
            System.out.println(errorMsg);
        } else {
            List<JsonObject> featuredItems = new ArrayList<>();
            JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
            JsonObject featuredObj = jo.getAsJsonObject("playlists");
            for (JsonElement featured : featuredObj.getAsJsonArray("items")) {
                featuredItems.add(featured.getAsJsonObject());
            }

            for (JsonObject featured : featuredItems) {
                System.out.println(featured.get("name").getAsString());
                System.out.println(featured.getAsJsonObject().get("external_urls")
                        .getAsJsonObject().get("spotify").getAsString() + "\n");
            }
        }
    }
}
