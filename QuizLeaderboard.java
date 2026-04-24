import java.net.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class QuizLeaderboard {

    public static void main(String[] args) throws Exception {

        String regNo = "AP23110010091";

        String baseUrl =
        "https://devapigw.vidalhealthtpa.com/srm-quiz-task/quiz/messages";

        String submitUrl =
        "https://devapigw.vidalhealthtpa.com/srm-quiz-task/quiz/submit";


        Set<String> uniqueEvents = new HashSet<>();
        Map<String,Integer> participantScores = new HashMap<>();


        for(int poll=0; poll<10; poll++)
        {

            String urlString =
            baseUrl + "?regNo=" + regNo + "&poll=" + poll;

            URL url = new URL(urlString);

            HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            BufferedReader reader =
            new BufferedReader(
            new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();

            String line;

            while((line = reader.readLine()) != null)
            {
                response.append(line);
            }

            reader.close();

            JSONObject jsonResponse =
            new JSONObject(response.toString());

            JSONArray events =
            jsonResponse.getJSONArray("events");


            for(int i=0;i<events.length();i++)
            {

                JSONObject event = events.getJSONObject(i);

                String roundId =
                event.getString("roundId");

                String participant =
                event.getString("participant");

                int score =
                event.getInt("score");


                String key =
                roundId + "_" + participant;


                if(!uniqueEvents.contains(key))
                {

                    uniqueEvents.add(key);

                    participantScores.put(
                    participant,
                    participantScores.getOrDefault(
                    participant,0)+score
                    );
                }
            }


            System.out.println(
            "Completed poll "+poll);


            Thread.sleep(5000);

        }


        List<Map.Entry<String,Integer>> leaderboard =
        new ArrayList<>(participantScores.entrySet());


        leaderboard.sort(
        (a,b)->b.getValue()-a.getValue()
        );


        JSONObject submitData =
        new JSONObject();

        submitData.put("regNo",regNo);


        JSONArray leaderboardArray =
        new JSONArray();


        for(Map.Entry<String,Integer> entry
        : leaderboard)
        {

            JSONObject obj =
            new JSONObject();

            obj.put("participant",
            entry.getKey());

            obj.put("totalScore",
            entry.getValue());

            leaderboardArray.put(obj);
        }


        submitData.put("leaderboard",
        leaderboardArray);


        URL submitURL =
        new URL(submitUrl);


        HttpURLConnection submitConn =
        (HttpURLConnection)
        submitURL.openConnection();


        submitConn.setRequestMethod("POST");

        submitConn.setRequestProperty(
        "Content-Type",
        "application/json"
        );

        submitConn.setDoOutput(true);


        OutputStream os =
        submitConn.getOutputStream();

        os.write(
        submitData.toString().getBytes()
        );

        os.flush();


        BufferedReader br =
        new BufferedReader(
        new InputStreamReader(
        submitConn.getInputStream()
        )
        );


        String output;

        while((output=br.readLine())!=null)
        {
            System.out.println(output);
        }


        br.close();

        System.out.println(
        "Leaderboard submitted successfully!");

    }
}