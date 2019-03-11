package com.upgrade.islandreservationsapi.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Configuration")
public class Configuration {

    public enum CONFIGURATION_NAMES {MAX_AVAILABILITY, MAX_RESERVATION, MIN_AHEAD, MAX_AHEAD, MAX_DATE_RANGE, DEFAULT_DATE_RANGE}

    @Column(name = "Name")
    @NotNull
    @Id
    private String name;
    @Column(name = "Description")
    private String description;
    @Column(name = "Value")
    @NotNull
    private String value;

    public Configuration() {}

    public Configuration(@NotNull String name, @NotNull String value) {
        this.name = name;
        this.value = value;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

