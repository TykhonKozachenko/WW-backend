package wander.wise.application.service.api.maps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import wander.wise.application.config.ApisConfigProperties;
import wander.wise.application.dto.maps.MapsResponseDto;
import wander.wise.application.exception.MapsException;

@Service
@RequiredArgsConstructor
public class MapsApiServiceImpl implements MapsApiService {
    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final double SCALE = Math.pow(10, 6);
    private final ApisConfigProperties apisConfigProperties;

    @Override
    public MapsResponseDto getMapsResponseByLocationName(String searchKey) {
        String formattedKey = searchKey.replaceAll(" ", "+");
        ResponseEntity<String> response = getMapsStringResponseEntity(formattedKey);
        JsonNode root = convertMapsResponseToJson(response);
        return parseCoordinatesAndCreateMapsResponseDto(formattedKey, root);
    }

    @Override
    public MapsResponseDto getMapsResponseByUsersUrl(String usersUrl) {
        //TODO: make order in this method
        String longUrl = null;
        double latitude = 0;
        double longitude = 0;
        try {
            longUrl = getRedirectUrl(usersUrl);
        } catch (IOException e) {
            throw new MapsException("Can't parse long url from the short one: "
                    + usersUrl, e);
        }
        if (longUrl.contains("@")) {
            int atSignIndex = longUrl.indexOf('@');
            String coordinatesString = longUrl.substring(atSignIndex + 1);
            String[] coordinatesArray = coordinatesString.split(",");
            if (coordinatesArray[1].contains("+")) {
                coordinatesArray[1] = coordinatesArray[1].replace("+", "");
            }
            latitude = Math.floor(Double.parseDouble(coordinatesArray[0]) * SCALE) / SCALE;
            longitude = Math.floor(Double.parseDouble(coordinatesArray[1]) * SCALE) / SCALE;
        } else {
            int questionMarkIndex = longUrl.indexOf("?");
            String coordinatesString = longUrl.substring(35, questionMarkIndex);
            String[] coordinatesArray = coordinatesString.split(",");
            if (coordinatesArray[1].contains("+")) {
                coordinatesArray[1] = coordinatesArray[1].replace("+", "");
            }
            latitude = Math.floor(Double.parseDouble(coordinatesArray[0]) * SCALE) / SCALE;
            longitude = Math.floor(Double.parseDouble(coordinatesArray[1]) * SCALE) / SCALE;
        }
        return new MapsResponseDto(longUrl, latitude, longitude);
    }

    private String getRedirectUrl(String usersUrl) throws IOException {
        InputStream inputStream = null;
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            URL url = new URL(usersUrl);
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            https.setInstanceFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);
            https.connect();
            int responseCode = https.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = https.getInputStream();
                return https.getURL().toString();
            }
        } catch (IOException e) {
            throw new MapsException("Something went wrong when parsing long url", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return null;
    }

    private ResponseEntity<String> getMapsStringResponseEntity(String formattedKey) {
        String geocodingLink = GEOCODING_API_URL
                + "?address=" + formattedKey
                + "&key=" + apisConfigProperties.mapsApiKey();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(geocodingLink, String.class);
        return response;
    }

    private static MapsResponseDto parseCoordinatesAndCreateMapsResponseDto(
            String formattedKey, JsonNode root) {
        String mapLink = "https://maps.google.com/maps?q=" + formattedKey;
        JsonNode location = root.path("results").path(0).path("geometry").path("location");
        double latitude = Math.floor(location.path("lat").asDouble() * SCALE) / SCALE;
        double longitude = Math.floor(location.path("lng").asDouble() * SCALE) / SCALE;
        return new MapsResponseDto(mapLink, latitude, longitude);
    }

    private static JsonNode convertMapsResponseToJson(ResponseEntity<String> response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode mapsResponseJson;
        try {
            mapsResponseJson = mapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new MapsException("Exception occurred when trying to map response to json: "
                    + response, e);
        }
        return mapsResponseJson;
    }
}
