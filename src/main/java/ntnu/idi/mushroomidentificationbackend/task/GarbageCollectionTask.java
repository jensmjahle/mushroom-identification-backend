package ntnu.idi.mushroomidentificationbackend.task;

import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ntnu.idi.mushroomidentificationbackend.service.GarbageCollectionService;

import java.util.logging.Logger;

/**
 * Scheduled task that performs garbage collection on the database.
 * This task runs on the 1st day of every month at midnight (00:00).
 * It deletes outdated records from the database to maintain performance and storage efficiency.
 * The task is configured to delete records older than a specified threshold (default is 6 months).
 */
@AllArgsConstructor
@Component
public class GarbageCollectionTask {

    private final Logger logger = Logger.getLogger(GarbageCollectionTask.class.getName());
    private final GarbageCollectionService garbageCollectionService;

    /**
     * This task runs every 1st day of the month at midnight (00:00).
     * It triggers the database garbage collection process to delete outdated records.
     * The cron expression can be configured via application properties.
     * The task is configured to delete records older than a specified threshold (default is 6 months).
     */
    @Scheduled(cron = "${garbage.collection.cron.expression:0 0 0 1 * *}")
    public void deleteOutdatedRequests() {
        try {
            LogHelper.info(logger, "Starting database garbage collection task...");
            garbageCollectionService.deleteOutdatedData(0); // Adjust the month threshold as needed
            LogHelper.info(logger, "Garbage collection completed successfully.");
        } catch (Exception e) {
            LogHelper.severe(logger, "Error during garbage collection: {0}", e.getMessage());
        }
    }

    /**
     * Runs daily at 02:00.
     * Scans the local 'uploads/' directory for any request‐ID folders
     * that no longer exist in the database and deletes them.
     */
    @Scheduled(cron = "${orphan.images.cron.expression:0 0 2 * * *}")
    public void cleanupOrphanImageDirectories() {
        try {
            LogHelper.info(logger, "Starting orphan‐image‐directory cleanup task...");
            garbageCollectionService.cleanupOrphanImageDirs();
            LogHelper.info(logger, "Orphan‐image‐directory cleanup completed successfully.");
        } catch (Exception e) {
            LogHelper.severe(logger, "Error during orphan‐image cleanup: {0}", e.getMessage());
        }
    }
}
