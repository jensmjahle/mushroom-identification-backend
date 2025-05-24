package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

@Service
public class DatabaseGarbageCollectionService {

    private final Logger logger = Logger.getLogger(DatabaseGarbageCollectionService.class.getName());
    private final AdminRepository adminRepository;
    private final MessageRepository messageRepository;
    private final UserRequestRepository userRequestRepository;

    @Autowired
    public DatabaseGarbageCollectionService(AdminRepository adminRepository, 
                                            MessageRepository messageRepository, 
                                            UserRequestRepository userRequestRepository) {
        this.adminRepository = adminRepository;
        this.messageRepository = messageRepository;
        this.userRequestRepository = userRequestRepository;
    }

    /**
     * Deletes outdated data from the database for Admin, Message, and UserRequest tables.
     * The outdated data is considered to be older than 6 months from the current date.
     * Admin is not included in the deletion by default.
     */
    public void deleteOutdatedData(int monthThreshold) {
        try {
            // Calculate the date that is 6 months ago from the current date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -monthThreshold); 
            Date dateThreshold = calendar.getTime();

            // Log the start of the garbage collection task
            logger.info("Starting garbage collection for data older than: " + dateThreshold);

            // Call the repository methods to delete outdated data
            // int deletedAdminCount = adminRepository.deleteByCreatedAtBefore(dateThreshold);
            int deletedMessageCount = messageRepository.deleteByCreatedAtBefore(dateThreshold);
            int deletedUserRequestCount = userRequestRepository.deleteByCreatedAtBefore(dateThreshold);

            // Log the number of records deleted from each table
            // logger.info("Deleted " + deletedAdminCount + " outdated Admin records.");
            logger.info("Deleted " + deletedMessageCount + " outdated Message records.");
            logger.info("Deleted " + deletedUserRequestCount + " outdated UserRequest records.");
        } catch (Exception e) {
            // Log any exceptions that occur during the garbage collection process
            logger.severe("Error during garbage collection: " + e.getMessage());
        }
    }
}
