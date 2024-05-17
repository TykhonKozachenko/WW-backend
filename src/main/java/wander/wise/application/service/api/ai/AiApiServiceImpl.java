package wander.wise.application.service.api.ai;

import static wander.wise.application.constants.AiApiServiceConstants.CLIMATE_LIST;
import static wander.wise.application.constants.AiApiServiceConstants.FULL_NAME_EXAMPLES;
import static wander.wise.application.constants.AiApiServiceConstants.FULL_NAME_RULES;
import static wander.wise.application.constants.AiApiServiceConstants.FULL_NAME_TEMPLATE;
import static wander.wise.application.constants.AiApiServiceConstants.LIST_FORMATING_RULES;
import static wander.wise.application.constants.AiApiServiceConstants.LOCATION_NAMES_FIELD_FORMAT;
import static wander.wise.application.constants.AiApiServiceConstants.MAPPER;
import static wander.wise.application.constants.AiApiServiceConstants.NON_EXISTING_RESTRICT;
import static wander.wise.application.constants.AiApiServiceConstants.SEPARATOR;
import static wander.wise.application.constants.AiApiServiceConstants.SPECIAL_REQUIREMENTS_LIST;
import static wander.wise.application.constants.AiApiServiceConstants.SPECIFIC_LOCATION_EXAMPLES;
import static wander.wise.application.constants.AiApiServiceConstants.TOTAL_REQUIRED_RESPONSES_AMOUNT;
import static wander.wise.application.constants.AiApiServiceConstants.TRIP_TYPES_LIST;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import wander.wise.application.exception.custom.AiException;

@Service
@RequiredArgsConstructor
public class AiApiServiceImpl implements AiApiService {
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

    @Override
    public CardSearchParameters defineRegion(CardSearchParameters searchParameters) {
        String paramsJson = null;
        try {
            paramsJson = MAPPER.writeValueAsString(searchParameters);
            System.out.println(paramsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid search parameters.", e);
        }
        String defineRegionPrompt = getDefineRegionPrompt(searchParameters, paramsJson);
        String response = chatClient.call(defineRegionPrompt);
        try {
            return MAPPER.readValue(response, CardSearchParameters.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ai returned incorrect search parameters: " + response, e);
        }
    }

    @Override
    public CardSearchParameters defineContinent(CardSearchParameters searchParameters) {
        String paramsJson = null;
        try {
            paramsJson = MAPPER.writeValueAsString(searchParameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid search parameters.", e);
        }
        String detectDistancePrompt = getDefineContinentPrompt(searchParameters, paramsJson);
        String response = chatClient.call(detectDistancePrompt);
        try {
            searchParameters = MAPPER.readValue(response, CardSearchParameters.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ai returned incorrect search parameters: " + response, e);
        }
        return searchParameters;
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
                .append("3. Each location should match this format: ")
                .append("Location name|Populated locality|Region|Country|Continent")
                .append(SEPARATOR)
                .append("Fix location name, if it doesn't match required pattern.")
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

    private static String getDefineRegionPrompt(CardSearchParameters searchParameters,
                                                String paramsJson) {
        return new StringBuilder()
                .append("I have this json object with search parameters: ")
                .append(paramsJson)
                .append(SEPARATOR)
                .append("Find in which region of ")
                .append(searchParameters.startLocation().split(",")[1])
                .append(" the ")
                .append(searchParameters.startLocation().split(",")[0])
                .append(" is situated, set name of this region in travel distance ")
                .append("field and return new object in the same format.")
                .append(SEPARATOR)
                .append("It is important to use local name of the region. ")
                .append("Good examples: Île-de-France, Kharkiv oblast, New York state, ect.")
                .toString();
    }

    private static String getDefineContinentPrompt(CardSearchParameters searchParameters,
                                                   String paramsJson) {
        return new StringBuilder()
                .append("I have this json object with search parameters: ")
                .append(paramsJson)
                .append(SEPARATOR)
                .append("Find on what continent is ")
                .append(searchParameters.startLocation().split(",")[1])
                .append(" located?, set name of this continent in travel distance ")
                .append("field and return new object in the same format.")
                .toString();
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
