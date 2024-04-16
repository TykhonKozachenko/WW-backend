package wander.wise.application.service.api.maps;

import wander.wise.application.dto.maps.MapsResponseDto;

public interface MapsApiService {
    MapsResponseDto getMapsResponseByLocationName(String locationName);

    MapsResponseDto getMapsResponseByUsersUrl(String usersUrl);
}
