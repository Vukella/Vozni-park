package com.example.vozni_park.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationId implements Serializable {
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "location_id")
    private Long locationId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLocationId that = (UserLocationId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(locationId, that.locationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, locationId);
    }
}
