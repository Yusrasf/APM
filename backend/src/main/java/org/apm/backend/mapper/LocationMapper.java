package org.apm.backend.mapper;

import org.apm.backend.dto.practitioner.LocationDTO;
import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationDTO toLocationDTO(Location location) {
        if (location == null) {
            return null;
        }

        LocationDTO dto = new LocationDTO();

        // id
        if (location.getIdElement() != null) {
            dto.setLocationId(location.getIdElement().getIdPart());
        }

        // name
        dto.setName(location.getName());

        // simple address: first line of the address, if present
        Address addr = location.getAddress();
        if (addr != null && addr.hasLine() && !addr.getLine().isEmpty()) {
            dto.setAddressLine(addr.getLine().get(0).getValue());
        }

        return dto;
    }
}
