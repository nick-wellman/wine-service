package com.nickwellman.wineservice.service;

import com.nickwellman.collections.Nucleus;
import com.nickwellman.collections.repository.GSARepository;
import com.nickwellman.collections.repository.MutableRepositoryItem;
import com.nickwellman.collections.repository.RepositoryItem;
import com.nickwellman.wineservice.config.WineNotesConstants;
import com.nickwellman.wineservice.models.WineNoteOutput;
import com.nickwellman.wineservice.models.WineNoteRequest;
import com.nickwellman.wineservice.models.WineNoteResponse;
import com.nickwellman.wineservice.models.WineNoteUpsert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class WineNotesService {
    private static final String REPO_NAME = "WineTastingRepository";

    private final GSARepository wineRepository;

    public WineNotesService() {
        wineRepository = (GSARepository) Nucleus.getInstance().getGenericService(REPO_NAME);
    }

    public WineNoteResponse getAllWineNotes() throws SQLException {
        final List<RepositoryItem> items = wineRepository.getAllRepositoryItems(WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
        final List<WineNoteOutput> wineNotes = items.stream().map(WineNotesService::convertRepositoryItemToWineNoteResponse).collect(toList());

        return WineNoteResponse.builder().wineNotes(wineNotes).build();
    }

    public WineNoteResponse getWineNotesByUser(final String user) throws SQLException {
        final List<RepositoryItem> items = wineRepository.getRepositoryItems(user, WineNotesConstants.PROPERTY_USER, WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
        final List<WineNoteOutput> wineNotes = items.stream().map(WineNotesService::convertRepositoryItemToWineNoteResponse).collect(toList());

        return WineNoteResponse.builder().wineNotes(wineNotes).build();
    }

    public WineNoteResponse getWineNotesByWineId(final int wineId) throws SQLException {
        final List<RepositoryItem> items = wineRepository.getRepositoryItems(Integer.toString(wineId), WineNotesConstants.PROPERTY_WINE_ID, WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
        final List<WineNoteOutput> wineNotes = items.stream().map(WineNotesService::convertRepositoryItemToWineNoteResponse).collect(toList());

        return WineNoteResponse.builder().wineNotes(wineNotes).build();
    }

    public WineNoteResponse getWineNotesByWineIdByUser(final int wineId, final String user) throws SQLException {
        final List<RepositoryItem> items = wineRepository.getRepositoryItems(Integer.toString(wineId), WineNotesConstants.PROPERTY_WINE_ID, WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
        final List<WineNoteOutput> wineNotes = items.stream()
                                                    .filter(ri -> ri.getPropertyValue(WineNotesConstants.PROPERTY_USER).equals(user))
                                                    .map(WineNotesService::convertRepositoryItemToWineNoteResponse)
                                                    .collect(toList());

        return WineNoteResponse.builder().wineNotes(wineNotes).build();
    }

    public WineNoteResponse addWineNotes(final WineNoteRequest request) throws SQLException {
        final WineNoteResponse response = new WineNoteResponse();
        List<RepositoryItem> items = wineRepository.getRepositoryItems(Integer.toString(request.getWineId()),
                                                                       WineNotesConstants.PROPERTY_WINE_ID,
                                                                       WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
        int maxOrdinal = items.stream().mapToInt(item -> (Integer) item.getPropertyValue(WineNotesConstants.PROPERTY_ORDINAL)).max().orElseGet(() -> -1);

        for (final String note : request.getWineNotes()) {
            final MutableRepositoryItem item = wineRepository.createItem(WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
            item.setProperty(WineNotesConstants.PROPERTY_WINE_ID, request.getWineId());
            item.setProperty(WineNotesConstants.PROPERTY_USER, request.getUser());
            item.setProperty(WineNotesConstants.PROPERTY_DATE, request.getDate());
            item.setProperty(WineNotesConstants.PROPERTY_NOTE, note);
            item.setProperty(WineNotesConstants.PROPERTY_ORDINAL, ++maxOrdinal);

            addItem(item);
        }

        for (final RepositoryItem item : items) {
            final MutableRepositoryItem mItem = (MutableRepositoryItem) item;
            final Optional<WineNoteUpsert> optional = request.getUpsert()
                                                             .stream()
                                                             .filter(i -> mItem.getPropertyValue(WineNotesConstants.PROPERTY_ID)
                                                                               .equals(Integer.parseInt(i.getId())))
                                                             .findFirst();
            if (optional.isPresent()) {
                log.info("found optional");
                mItem.setProperty(WineNotesConstants.PROPERTY_NOTE, optional.get().getNote());
                wineRepository.updateItem(mItem, WineNotesConstants.PROPERTY_NOTE);
            }
        }

        items = wineRepository.getRepositoryItems(Integer.toString(request.getWineId()), WineNotesConstants.PROPERTY_WINE_ID, WineNotesConstants.WINE_NOTES_REPOSITORY_ITEM);
        for (final RepositoryItem item : items) {
            response.getWineNotes().add(convertRepositoryItemToWineNoteResponse(item));
        }

        return response;
    }

    private void addItem(final MutableRepositoryItem item) {
        try {
            wineRepository.addItem(item);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static WineNoteOutput convertRepositoryItemToWineNoteResponse(final RepositoryItem repositoryItem) {
        final WineNoteOutput output = new WineNoteOutput();
        output.setId((Integer) repositoryItem.getPropertyValue(WineNotesConstants.PROPERTY_ID));
        output.setWineId((Integer) repositoryItem.getPropertyValue(WineNotesConstants.PROPERTY_WINE_ID));
        output.setUser((String) repositoryItem.getPropertyValue(WineNotesConstants.PROPERTY_USER));
        output.setNote((String) repositoryItem.getPropertyValue(WineNotesConstants.PROPERTY_NOTE));
        output.setOrdinal((Integer) repositoryItem.getPropertyValue(WineNotesConstants.PROPERTY_ORDINAL));
        output.setDate((String) repositoryItem.getPropertyValue(WineNotesConstants.PROPERTY_DATE));
        return output;
    }
}
