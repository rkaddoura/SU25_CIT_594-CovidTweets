package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.DataShare;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONReader extends DataReader {

    protected JSONReader(String filename, DataType dataType) {
        this.filename = filename;
        this.dataType = dataType;
    }

    @Override
    public void readFile() throws IOException {

        Object obj = null;

        try {
            obj = new JSONParser().parse(new FileReader(this.filename));
        } catch (IOException | ParseException e) {
            throw new IOException(e);
        }

        JSONArray ja = (JSONArray) obj;

        List<CovidRecord> covidRecords = new LinkedList<>();

        String zipRegex = "^\\d{5}$";
        Pattern zipPattern = Pattern.compile(zipRegex);
        String timeRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
        Pattern timePattern = Pattern.compile(timeRegex);

        for (Object object : ja) {

            JSONObject jo = (JSONObject) object;

            CovidRecord cv = new CovidRecord();

            // validate zipcode & timestamp
            if (!jo.containsKey("zip_code") || !jo.containsKey("etl_timestamp")){
                continue;
            }

            String covZIP = jo.get("zip_code").toString().trim();
            Matcher zipMatcher = zipPattern.matcher(covZIP);
            if (!zipMatcher.matches()){
                continue;
            }

            String covTime= jo.get("etl_timestamp").toString().trim();
            Matcher timeMatcher = timePattern.matcher(covTime);
            if (!timeMatcher.matches()){
                continue;
            }

            cv.setZipCode(Integer.parseInt(covZIP));
            cv.setEtlTimestamp(covTime);

            if (jo.containsKey("partially_vaccinated")) {
                cv.setPartiallyVaccinated(parseIntValue(jo.get("partially_vaccinated").toString()));
            }
            if (jo.containsKey("fully_vaccinated")) {
                cv.setFullyVaccinated(parseIntValue(jo.get("fully_vaccinated").toString()));
            }

            covidRecords.add(cv);

        }
        DataShare ds = DataShare.getInstance();
        ds.setCovidData(covidRecords);
    }


}
