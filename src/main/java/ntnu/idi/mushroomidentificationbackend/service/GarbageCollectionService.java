package ntnu.idi.mushroomidentificationbackend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for performing garbage collection on the database.
 */
@Service
public class GarbageCollectionService {

    private final Logger logger = Logger.getLogger(GarbageCollectionService.class.getName());
    private final UserRequestRepository userRequestRepository;
    private static final Path UPLOAD_ROOT = Paths.get("uploads");

    @Autowired
    public GarbageCollectionService(UserRequestRepository userRequestRepository) {
        this.userRequestRepository = userRequestRepository;
    }

    /**
     * Deletes outdated data from the database for Admin, Message, and UserRequest tables.
     * The outdated data is considered to be older than 6 months from the current date.
     * Admin is not included in the deletion by default.
     */
    @Transactional
    public void deleteOutdatedData(int monthThreshold) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -monthThreshold); 
            Date dateThreshold = calendar.getTime();
            LogHelper.info(logger, "Starting garbage collection for data older than: {0}", dateThreshold);

            List<UserRequest> outdatedUserRequests = userRequestRepository.findByCreatedAtBefore(dateThreshold);
            for (UserRequest req : outdatedUserRequests) {
                deleteImagesForRequest(req.getUserRequestId());
            }
            int deletedUserRequestCount = userRequestRepository.deleteByCreatedAtBefore(dateThreshold);
            LogHelper.info(logger, "Deleted {0} outdated user requests older than {1}", 
                deletedUserRequestCount, dateThreshold);
        } catch (Exception e) {
            LogHelper.severe(logger, "Error during garbage collection: {0}", e.getMessage());
        }
    }
    
    private void deleteImagesForRequest(String userRequestId) {
        try {
            ImageService.deleteImagesForRequest(userRequestId);
        } catch (IOException ioe) {
            LogHelper.severe(logger, "Error deleting images for request {0}: {1}",
                userRequestId, ioe.getMessage());
        }
    }


    /**
     * Sweep for orphaned upload directories (no corresponding DB row).
     * Any subdirectory under uploads/ whose (sanitised) name does not match an existing
     * UserRequest ID will be deleted recursively.
     */
    public void cleanupOrphanImageDirs() {
        if (!Files.exists(UPLOAD_ROOT) || !Files.isDirectory(UPLOAD_ROOT)) {
            LogHelper.warning(logger, "Upload root does not exist or is not a directory: {0}", UPLOAD_ROOT);
            return;
        }

        try (Stream<Path> dirs = Files.list(UPLOAD_ROOT)) {
            dirs.filter(Files::isDirectory)
                .forEach(dir -> {
                    String rawId = dir.getFileName().toString();
                    String safeId = rawId.replaceAll("[^a-zA-Z0-9_-]", "_");
                    boolean existsInDb = userRequestRepository.existsById(safeId);

                    if (!existsInDb) {
                        LogHelper.info(logger, "Orphan directory detected, deleting: {0}", dir);
                        try (Stream<Path> paths = Files.walk(dir)) {
                            paths.sorted(Comparator.reverseOrder())
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                        LogHelper.info(logger, "Deleted: {0}", path);
                                    } catch (IOException ioe) {
                                        LogHelper.warning(logger, "Failed to delete {0}: {1}", path, ioe.getMessage());
                                    }
                                });
                        } catch (IOException walkEx) {
                            LogHelper.warning(logger, "Error traversing orphan dir {0}: {1}", dir, walkEx.getMessage());
                        }
                    }
                });
        } catch (IOException listEx) {
            LogHelper.warning(logger, "Failed to list upload root {0}: {1}", UPLOAD_ROOT, listEx.getMessage());
        }
    }
}
