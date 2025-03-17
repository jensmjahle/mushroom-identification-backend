package ntnu.idi.mushroomidentificationbackend.task;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ntnu.idi.mushroomidentificationbackend.service.DatabaseGarbageCollectionService;

import java.util.logging.Logger;

@AllArgsConstructor
@Component
public class DatabaseGarbageCollectionTask {

    private final Logger logger = Logger.getLogger(DatabaseGarbageCollectionTask.class.getName());
    private final DatabaseGarbageCollectionService garbageCollectionService;

    /**
     * This task runs every 1st day of the month at midnight (00:00).
     * It triggers the database garbage collection process to delete outdated records.
     */
    @Scheduled(cron = "${garbage.collection.cron.expression:0 0 0 1 * *}")
    public void runGarbageCollection() {
        try {
            // Logging start of the garbage collection task
            logger.info("Running database garbage collection task...");

            // Call the service to delete outdated data
            garbageCollectionService.deleteOutdatedData();

            // Logging successful completion
            logger.info("Garbage collection completed successfully.");
        } catch (Exception e) {
            // Logging any errors that occur during the garbage collection process
            logger.severe("Error during garbage collection: " + e.getMessage());
        }
    }
}
