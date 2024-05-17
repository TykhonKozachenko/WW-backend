package wander.wise.application.constants;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AiApiServiceConstants {
    public static final String CLIMATE_LIST = "Tropical|Polar|Temperate";
    public static final String FULL_NAME_EXAMPLES =
            "Examples: Central park|New York|New York state|USA|North America, "
                    + "Freedom Square|Kharkiv|Kharkiv Oblast|Ukraine|Europe, "
                    + "Louvre|Paris|Île-de-France|France|Europe)\",";
    public static final String FULL_NAME_RULES = "(Double check this field. "
            + "Fill in each point. Use | between points. ";
    public static final String FULL_NAME_TEMPLATE = "\"location name|populated locality|"
            + "region|country|continent";
    public static final String LIST_FORMATING_RULES = "Give me the list of "
            + "location. Return information as "
            + "json object. Use this format: ";
    public static final String LOCATION_NAMES_FIELD_FORMAT = "\"locationNames\": "
            + "[\"Location name1|Populated locality|Region|Country|Continent\", "
            + "\"Location name 2|Populated locality|Region|Country|Continent\", "
            + "\"Location name 3|Populated locality|Region|Country|Continent\", ect.]";
    public static final String NON_EXISTING_RESTRICT = "It is important that"
            + " the locations exist. "
            + "I will be in danger if travel to non-existing location.";
    public static final String SEPARATOR = System.lineSeparator();
    public static final String SPECIAL_REQUIREMENTS_LIST = "With pets|With kids|"
            + "LGBTQ friendly|Disability.";
    public static final String SPECIFIC_LOCATION_EXAMPLES = "Examples: square, museum, market, "
            + "mall, park, certain mountain, bridge, theater, lake, "
            + "embankment, castle etc.";
    public static final String TRIP_TYPES_LIST = "Active|Chill|Native culture|"
            + "Family|Culture|Spiritual|Extreme|Corporate|Nature|Shopping|Romantic|Party";
    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final int TOTAL_REQUIRED_RESPONSES_AMOUNT = 30;
}
