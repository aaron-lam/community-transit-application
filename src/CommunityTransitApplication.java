import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunityTransitApplication {

    public static void main(String[] args) throws Exception {
        String url = "https://www.communitytransit.org/busservice/schedules/";
        URLConnection mainURLConnection = getConnection(url);
        String text = readWebsiteContent(mainURLConnection);
        char initial = getDestinationsFirstLetter();
        printStopsAndRouteNumbers(text, initial);
        String routeId = getRouteId();

        URLConnection routeURLConnection = getConnection(url + "route/" + routeId);
        String routeURLText = readWebsiteContent(routeURLConnection);
        System.out.println("\nThe link for your route is: " + url + "route/" + routeId + "\n");
        printDestinationAndStops(routeURLText);
    }

    private static URLConnection getConnection(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("user-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        return connection;
    }

    private static String readWebsiteContent(URLConnection connection) throws IOException {
        BufferedReader reader;
        StringBuilder text = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            text.append(inputLine).append("\n");
        }
        reader.close();
        return text.toString();
    }

    /**
     * Get the first letter of destination from the user input.
     */
    private static char getDestinationsFirstLetter() {
        Scanner scanner = new Scanner(System.in);
        char firstLetter;
        while (true) {
            System.out.print("Please enter a letter that your destinations start with: ");
            String input = scanner.next();
            if (input.length() == 1) {
                firstLetter = input.charAt(0);
                break;
            }
            System.out.println("Please enter only one letter.");
        }
        return firstLetter;
    }

    /**
     * Print all information of the specific cities.
     */
    private static void printStopsAndRouteNumbers(String text, char initial) {
        Pattern wholePart = Pattern.compile("<h3>(" + initial + ".*)</h3>\\s+" +
                "(<div.*>\\s+<div.*>\\s+<strong><a.*>(.*)</a></strong>\\s+</div>\\s+<div.*</div>\\s+</div>\\s+)+");
        Matcher wholePartMatcher = wholePart.matcher(text);
        TreeMap<String, List<String>> map = new TreeMap<>();

        // put destination and bus number in the map
        while (wholePartMatcher.find()) {
            String destination = wholePartMatcher.group(1);
            String divStr = wholePartMatcher.group().replace("<h3>" + destination + "</h3>", "");
            String[] divArr = divStr.split("<div.*>.*</div>");
            List<String> busNumbers = getAllBusNumber(divArr);
            map.put(destination, busNumbers);
        }

        // print the destination and bus number from map
        for (String destination : map.keySet()) {
            System.out.println("Destination: " + destination);
            for (String busNumber : map.get(destination)) {
                System.out.println("Bus Number: " + busNumber);
            }
            System.out.println("+++++++++++++++++++++++++++");
        }
    }

    /**
     * Put all bus number to the list.
     */
    private static List<String> getAllBusNumber(String[] divArr) {
        List<String> busNumbers = new ArrayList<>();
        new LinkedList<>();
        for (String divContent : divArr) {
            Pattern busNumberPattern = Pattern.compile("<a.*>(.*)</a>");
            Matcher busNumberMatcher = busNumberPattern.matcher(divContent);
            while (busNumberMatcher.find()) {
                busNumbers.add(busNumberMatcher.group(1));
            }
        }
        return busNumbers;
    }

    /**
     * Get the route id from user input
     */
    private static String getRouteId() {
        Scanner scanner = new Scanner(System.in);
        String routeId;
        while (true) {
            System.out.print("Please enter a route ID as a string: ");
            routeId = scanner.next();
            if (routeId.matches("-?\\d+")) {
                break;
            }
            System.out.println("Please enter only valid route ID.");
        }
        return routeId;
    }

    /**
     * Print all information of the requested route id's destinations and stops.
     */
    private static void printDestinationAndStops(String routeURLText) {
        Pattern wholePart = Pattern.compile("<h2>Weekday<small>(.*)</small></h2>\\s+</td>\\s+</tr>\\s+<tr>\\s+" +
                "(<th.*>\\s+<span.*>\\s+<i.*></i>\\s+<strong.*>.*</strong>\\s+</span>\\s+<p>(.*)</p>\\s+</th>\\s+)+");
        Matcher wholePartMatcher = wholePart.matcher(routeURLText);
        while (wholePartMatcher.find()) {
            String destination = wholePartMatcher.group(1);
            String tableHeadStr = wholePartMatcher.group().replaceAll("<h2>Weekday<small>" + destination + "</small></h2>\\s+</td>\\s+</tr>\\s+<tr>\\s+", "");
            // stopsArr store all stop names in the specific route
            String[] stopsArr = tableHeadStr.split("<th.*>\\s+<span.*>\\s+<i.*></i>\\s+<strong.*>.*</strong>\\s+</span>\\s+<p>");
            // stopsNumArr store all stop numbers in the specific route
            String[] stopsNumArr = tableHeadStr.split("<th.*>\\s+<span.*>\\s+<i.*></i>\\s+");
            System.out.println("Destination: " + destination);
            for (int i = 0; i < stopsArr.length; i++) {
                // handle exception case
                if (stopsArr[i] == null || stopsArr[i].length() == 0) {
                    continue;
                }
                String stop = getStopName(stopsArr[i]);
                String stopNum = getStopNumber(stopsNumArr[i]);
                System.out.println("Stop number: " + stopNum + " is " + stop);
            }
            System.out.println("+++++++++++++++++++++++++++");
        }
    }

    /**
     * Get stop name from raw text string.
     */
    private static String getStopName(String stopName) {
        int index = stopName.indexOf("<");
        stopName = stopName.substring(0, index);
        return stopName.replace("amp;", "");
    }

    /**
     * Get stop number from raw text string.
     */
    private static String getStopNumber(String stopNum) {
        stopNum = stopNum.replace("<strong class=\"fa fa-stack-1x\">", "");
        int index = stopNum.indexOf("<");
        return stopNum.substring(0, index);
    }
}
