package com.upgrade.islandreservationsapi.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "configuration")
public class Configuration {

    public enum CONFIGURATION_NAMES {MAX_AVAILABILITY, MAX_RESERVATION, MIN_AHEAD, MAX_AHEAD}

    @NotNull
    @Id
    @Enumerated(value = EnumType.STRING)
    private String name;
    @NotNull
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

