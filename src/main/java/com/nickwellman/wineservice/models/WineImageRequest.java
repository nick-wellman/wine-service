package com.nickwellman.wineservice.models;

import lombok.Data;

@Data
public class WineImageRequest {
    private int wineId;
    private String label;
}
