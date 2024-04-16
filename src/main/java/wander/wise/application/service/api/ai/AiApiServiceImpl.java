package wander.wise.application.service.api.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;
import wander.wise.application.dto.ai.AiResponseDto;
import wander.wise.application.dto.ai.LocationListDto;
import wander.wise.application.dto.card.CardSearchParameters;
import wander.wise.application.exception.AiException;

@Service
@RequiredArgsConstructor
public class AiApiServiceImpl implements AiApiService {
    private static final String CLIMATE_LIST = "Tropical|Polar|Temperate";
    private static final String FULL_NAME_EXAMPLES =
            "Examples: Central park|New York|New York state|USA|North America, "
                    + "Blagoveshchensky cathedral|Kharkiv|Kharkiv Oblast|Ukraine|Europe)\",";
    private static final String FULL_NAME_RULES = "(Double check this field. "
            + "Fill in each point. Use | between points. ";
    private static final String FULL_NAME_TEMPLATE = "\"location name|populated locality|"
            + "region|country|continent";
    private static final String LIST_FORMATING_RULES = "Give me the list of "
            + "location. Return information as "
            + "json object. Use this format: ";
    private static final String LOCATION_NAMES_FIELD_FORMAT = "\"locationNames\": "
            + "[\"location name1|populated locality|region|country|continent\", "
            + "\"location name 2|populated locality|region|country|continent\", "
            + "\"location name 3|populated locality|region|country|continent\", ect.]";
    private static final String NON_EXISTING_RESTRICT = "It is important that"
            + " the locations exist. "
            + "I will be in danger if travel to non-existing location.";
    private static final String SEPARATOR = System.lineSeparator();
    private static final String SPECIAL_REQUIREMENTS_LIST = "With pets|With kids|"
            + "LGBTQ friendly|Disability.";
    private static final String SPECIFIC_LOCATION_EXAMPLES = "Examples: square, museum, market, "
            + "mall, park, certain mountain, bridge, theater, lake, "
            + "embankment, castle etc.";
    private static final String TRIP_TYPES_LIST = "Active|Chill|Native culture|"
            + "Family|Culture|Spiritual|Extreme|Corporate|Nature|Shopping|Romantic|Party";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int TOTAL_REQUIRED_RESPONSES_AMOUNT = 30;
    private final ChatClient chatClient;

    @Override
    public List<AiResponseDto> getAiResponses(
            CardSearchParameters searchParameters,
            Map<String, List<String>> locationsToExcludeAndTypeMap) {
        List<AiResponseDto> aiResponses = new ArrayList<>();
        String totalLocationsToExclude = getTotalLocationsToExclude(locationsToExcludeAndTypeMap);
        locationsToExcludeAndTypeMap.keySet().forEach(tripType -> {
            String locationsToExclude = getLocationsToExclude(
                    locationsToExcludeAndTypeMap,
                    tripType);
            Set<String> generatedNamesByType = getLocationList(
                    searchParameters,
                    locationsToExcludeAndTypeMap,
                    tripType,
                    locationsToExclude,
                    totalLocationsToExclude);
            if (generatedNamesByType.size() > 0) {
                List<AiResponseDto> aiResponsesByType = initializeAiResponses(
                        searchParameters,
                        tripType,
                        generatedNamesByType);
                aiResponses.addAll(aiResponsesByType);
            }
        });
        return aiResponses
                .stream()
                .filter(aiResponseDto -> aiResponseDto
                        .fullName()
                        .contains(searchParameters
                                .travelDistance()[0]))
                .toList();
    }

    /**
     * Get checked and formated list of locations
     */
    private Set<String> getLocationList(
            CardSearchParameters searchParameters,
            Map<String, List<String>> locationsToExcludeAndTypeMap,
            String tripType, String locationsToExclude,
            String totalLocationsToExclude) {
        return getLocationListDto(
                searchParameters,
                locationsToExclude,
                totalLocationsToExclude,
                tripType,
                getResponsesAmount(locationsToExcludeAndTypeMap))
                .locationNames()
                .stream()
                .filter(name -> !totalLocationsToExclude.contains(name))
                .collect(Collectors.toSet());
    }

    /**
     * Initialize AiResponseDtos and complete "tripTypes"
     * and "specialRequirements" fields
     */
    private List<AiResponseDto> initializeAiResponses(
            CardSearchParameters searchParameters,
            String tripType,
            Set<String> generatedNamesByType) {
        List<AiResponseDto> aiResponsesByType = generatedNamesByType.stream()
                .map(this::generateLocationDetails)
                .map(aiResponseDto -> finishResponseDtoInitialization(
                        searchParameters,
                        tripType,
                        aiResponseDto))
                .toList();
        return aiResponsesByType;
    }

    private static AiResponseDto finishResponseDtoInitialization(
            CardSearchParameters searchParameters,
            String tripType,
            AiResponseDto aiResponseDto) {
        if (!aiResponseDto.tripTypes().contains(tripType)) {
            aiResponseDto = aiResponseDto.setTripTypes(aiResponseDto.tripTypes() + "|" + tripType);
        }
        for (String specialRequirement : searchParameters.specialRequirements()) {
            if (!aiResponseDto.specialRequirements().contains(specialRequirement)) {
                aiResponseDto = aiResponseDto.setSpecialRequirements(
                        aiResponseDto.specialRequirements()
                                + "|"
                                + specialRequirement);
            }
        }
        return aiResponseDto;
    }

    /**
     * ChatClient requests
     */
    private LocationListDto getLocationListDto(
            CardSearchParameters searchParameters,
            String locationsToExclude,
            String totalLocationsToExclude,
            String tripType,
            int responsesAmount) {
        LocationListDto locationListDto = null;
        String locationListPrompt = getListOfLocationsPrompt(
                searchParameters,
                locationsToExclude,
                tripType,
                responsesAmount);
        String locationList = chatClient.call(locationListPrompt);
        locationList = removeDuplicates(
                locationList,
                totalLocationsToExclude);
        locationList = check(locationList);
        try {
            locationListDto = MAPPER.readValue(locationList, LocationListDto.class);
        } catch (JsonProcessingException e) {
            throw new AiException("Ai returned invalid location list: "
                    + locationList, e);
        }
        return locationListDto;
    }

    private String removeDuplicates(String locationList, String totalLocationsToExclude) {
        String removeDuplicatesPrompt = getRemoveDuplicatesPrompt(
                locationList,
                totalLocationsToExclude);
        locationList = chatClient.call(removeDuplicatesPrompt);
        return locationList;
    }

    private String check(String locationList) {
        String checkWhereIsPrompt = getCheckPrompt(locationList);
        locationList = chatClient.call(checkWhereIsPrompt);
        return locationList;
    }

    private AiResponseDto generateLocationDetails(String name) {
        String locationDetailsPrompt = getLocationDetailsPrompt(name);
        String locationDetails = chatClient.call(locationDetailsPrompt);
        try {
            return MAPPER.readValue(locationDetails, AiResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new AiException("Ai returned invalid location details: "
                    + locationDetails, e);
        }
    }

    /**
     * Prompt generation
     */
    private String getListOfLocationsPrompt(CardSearchParameters searchParameters,
                                            String locationsToExclude, String tripType,
                                            int responsesAmount) {
        StringBuilder listOfLocationsPrompt = new StringBuilder();
        listOfLocationsPrompt.append("I am in ").append(searchParameters.startLocation())
                .append(SEPARATOR)
                .append("Find me " + responsesAmount
                        + " locations, where to travel by this requirements: ")
                .append(SEPARATOR)
                .append("Trip type: ").append(tripType)
                .append(SEPARATOR)
                .append("Climate: ");
        Arrays.stream(searchParameters.climate()).forEach(type
                -> listOfLocationsPrompt.append(type).append(", "));
        listOfLocationsPrompt.append("Special requirements: ");
        Arrays.stream(searchParameters.specialRequirements()).forEach(requirement
                        -> listOfLocationsPrompt.append(requirement).append(", "));
        listOfLocationsPrompt.append(SEPARATOR)
                .append("The result should not contain these locations: ")
                .append(locationsToExclude)
                .append(SEPARATOR)
                .append("Locations must be within: ")
                .append(searchParameters.travelDistance()[0])
                .append(". Collect locations from different parts of it. It is very "
                        + "important to fill the list with locations all around ")
                .append(searchParameters.travelDistance()[0])
                .append(SEPARATOR);
        listOfLocationsPrompt.append("It is very important to find specific locations. ")
                .append(SPECIFIC_LOCATION_EXAMPLES)
                .append(SEPARATOR)
                .append("Result should contain at least " + responsesAmount
                        + " locations. Better find more than " + responsesAmount + ".")
                .append(SEPARATOR);
        listOfLocationsPrompt.append(NON_EXISTING_RESTRICT)
                .append(SEPARATOR)
                .append(LIST_FORMATING_RULES)
                .append(SEPARATOR)
                .append("{")
                .append(SEPARATOR)
                .append(LOCATION_NAMES_FIELD_FORMAT)
                .append(SEPARATOR)
                .append("}");
        return listOfLocationsPrompt.toString();
    }

    private String getRemoveDuplicatesPrompt(String locationList,
                                             String locationsToExclude) {
        StringBuilder filterListPrompt = new StringBuilder();
        filterListPrompt
                .append("I have two lists of locations. The first here: ")
                .append(SEPARATOR)
                .append(locationsToExclude)
                .append(SEPARATOR)
                .append("The second text is a json file with locations, "
                        + "that has been generated by AI. Here: ")
                .append(SEPARATOR)
                .append(locationList)
                .append(SEPARATOR)
                .append("I need you delete duplicates from the second list by this algorithm: ")
                .append(SEPARATOR)
                .append("1. Delete locations that are present in the first list. Remove the "
                        + "same names and the names, that are very similar (if the first list "
                        + "is empty, skip this step).")
                .append(SEPARATOR)
                .append("2. If the location has several names and one of them is in the first "
                        + "list, remove this location from the second list (if the first list is "
                        + "empty, skip this step).")
                .append(SEPARATOR)
                .append("3. Use rules above to also delete duplicates,"
                        + " that the second list contains.")
                .append(SEPARATOR)
                .append("Return the second list in the same json format, "
                        + "in which you received it.");
        return filterListPrompt.toString();
    }

    private String getCheckPrompt(String locationList) {
        StringBuilder checkedLocations = new StringBuilder();
        checkedLocations.append("I have this list of locations in json: ")
                .append(SEPARATOR)
                .append(locationList)
                .append(SEPARATOR)
                .append("I need you to fix mistakes in this list"
                        + " of locations by next algorithm: ")
                .append(SEPARATOR)
                .append("1. If the location doesn't exist "
                        + "remove it from the list.")
                .append(SEPARATOR)
                .append("2. Locations are coupled with places "
                        + "in which they situated. Carefully "
                        + "check is each location really situated "
                        + "in this place. If not, fix the mistake.")
                .append(SEPARATOR)
                .append("Return the result in the same json format.");
        return checkedLocations.toString();
    }

    private String getLocationDetailsPrompt(String locationName) {
        StringBuilder locationDetailsPrompt = new StringBuilder();
        locationDetailsPrompt.append("I want to know more about this location: ")
                .append(locationName)
                .append(SEPARATOR)
                .append("Give me answer strictly as a json object. Use this format: ")
                .append(SEPARATOR)
                .append("{").append(SEPARATOR)
                .append("\"fullName\": ").append(FULL_NAME_TEMPLATE).append(FULL_NAME_RULES)
                .append(FULL_NAME_EXAMPLES)
                .append(SEPARATOR)
                .append("\"tripTypes\": ").append("\"(add several from this list: ")
                .append(TRIP_TYPES_LIST)
                .append(". Use | between points.")
                .append(")\",")
                .append(SEPARATOR)
                .append("\"climate\": \"one from this list: ").append(CLIMATE_LIST)
                .append(")\",")
                .append(SEPARATOR)
                .append("\"specialRequirements\": \"(add some from this list: ")
                .append(SPECIAL_REQUIREMENTS_LIST)
                .append(" Use | between points)\",")
                .append(SEPARATOR)
                .append("\"description\": \"(2-3 sentences)\",")
                .append(SEPARATOR)
                .append("\"whyThisPlace\": \"reason 1|reason 2|reason 3|"
                        + " (3-5 words per reason)\"")
                .append(SEPARATOR)
                .append("}");
        return locationDetailsPrompt.toString();
    }

    /**
     * Util methods
     */
    private static int getResponsesAmount(
            Map<String, List<String>> locationsToExcludeAndTypeMap) {
        return TOTAL_REQUIRED_RESPONSES_AMOUNT / locationsToExcludeAndTypeMap.size();
    }

    private static String getTotalLocationsToExclude(
            Map<String, List<String>> locationsToExcludeAndTypeMap) {
        return locationsToExcludeAndTypeMap.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.joining(", "));
    }

    private static String getLocationsToExclude(
            Map<String, List<String>> locationsToExcludeAndTypeMap,
            String tripType) {
        return locationsToExcludeAndTypeMap.get(tripType).toString();
    }
}
