package com.nickwellman.wineservice.models;

import lombok.Data;

@Data
public class WineryResponse {
    private int id;
    private String name;
    private String location;
}
