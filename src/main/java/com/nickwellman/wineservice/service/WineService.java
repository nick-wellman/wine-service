package com.nickwellman.wineservice.service;

import com.nickwellman.collections.Nucleus;
import com.nickwellman.collections.repository.GSARepository;
import com.nickwellman.collections.repository.MutableRepositoryItem;
import com.nickwellman.collections.repository.RepositoryItem;
import com.nickwellman.wineservice.config.WineConstants;
import com.nickwellman.wineservice.models.WineRequest;
import com.nickwellman.wineservice.models.WineResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class WineService {
    private static final String REPO_NAME = "WineTastingRepository";

    private final GSARepository wineRepository;

    public WineService() {
        wineRepository = (GSARepository) Nucleus.getInstance().getGenericService(REPO_NAME);
    }

    public List<WineResponse> getAllWines() throws SQLException {
        final List<RepositoryItem> items = wineRepository.getAllRepositoryItems(WineConstants.WINE_REPOSITORY_ITEM);
        return items.stream().map(WineService::convertRepositoryItemToWineResponse).collect(toList());
    }

    public List<WineResponse> getWinesByWineryId(final String id) throws SQLException {
        final List<RepositoryItem> items = wineRepository.getAllRepositoryItems(WineConstants.WINE_REPOSITORY_ITEM);
        return items.stream()
                    .filter(ri -> ri.getPropertyValue(WineConstants.PROPERTY_WINERY_ID).equals(Integer.parseInt(id)))
                    .map(WineService::convertRepositoryItemToWineResponse)
                    .sorted(Comparator.comparing(WineResponse::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(toList());
    }

    public WineResponse getWine(final String id) throws SQLException {
        final RepositoryItem item = wineRepository.getRepositoryItem(id, WineConstants.WINE_REPOSITORY_ITEM);
        return convertRepositoryItemToWineResponse(item);
    }

    public WineResponse addWine(final WineRequest request) throws SQLException {
        final MutableRepositoryItem item = wineRepository.createItem(WineConstants.WINE_REPOSITORY_ITEM);
        item.setProperty(WineConstants.PROPERTY_WINERY_ID, request.getWineryId());
        item.setProperty(WineConstants.PROPERTY_NAME, request.getName());
        item.setProperty(WineConstants.PROPERTY_STYLE, request.getStyle());
        final RepositoryItem addedItem = wineRepository.addItem(item);

        return convertRepositoryItemToWineResponse(addedItem);
    }

    private static WineResponse convertRepositoryItemToWineResponse(final RepositoryItem repositoryItem) {
        final WineResponse response = new WineResponse();
        response.setId((Integer) repositoryItem.getPropertyValue(WineConstants.PROPERTY_ID));
        response.setWineryId((Integer) repositoryItem.getPropertyValue(WineConstants.PROPERTY_WINERY_ID));
        response.setName((String) repositoryItem.getPropertyValue(WineConstants.PROPERTY_NAME));
        response.setStyle((String) repositoryItem.getPropertyValue(WineConstants.PROPERTY_STYLE));
        return response;
    }
}
