package ntnu.idi.mushroomidentificationbackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, String> {

  // Get paginated user requests by status sorted by updatedAt (ascending)
  Page<UserRequest> findByStatusOrderByUpdatedAtAsc(UserRequestStatus status, Pageable pageable);
  
  // Get paginated user requests for a specific admin by status, sorted by updatedAt (ascending)
  Page<UserRequest> findByAdminAndStatusOrderByUpdatedAtAsc(Admin admin, UserRequestStatus status, Pageable pageable);

  // Fetch paginated user requests sorted by updatedAt (newest first)
  Page<UserRequest> findAllByOrderByUpdatedAtDesc(Pageable pageable);
  

 Optional<UserRequest> findByPasswordHash(String passwordHash);

  Optional<UserRequest> findByUserRequestId(String userRequestId);

  Optional<UserRequest> findByLookUpKey(String referenceCode);

  int deleteByCreatedAtBefore(Date dateThreshold);

  Long countByStatus(UserRequestStatus status);

  List<UserRequest> findByCreatedAtBetween(Date createdAt, Date createdAt2);

  long countByCreatedAtBetween(Date createdAt, Date createdAt2);

  long countByStatusAndCreatedAtBetween(UserRequestStatus status, Date createdAt, Date createdAt2);

  Page<UserRequest> findAllByStatus(UserRequestStatus status, Pageable pageable);
  Page<UserRequest> findAllByStatusNot(UserRequestStatus status, Pageable pageable);

  /**
   * Find the first user request by status, ordered by updatedAt in ascending order.
   * Used for fetching the oldest request of a specific status.
   * Picking the next request from the queue.
   *
   * @param status the status of the user request to filter by
   * @return an Optional containing the first user request with the specified status, or an empty Optional if none found
   */
  Optional<UserRequest> findFirstByStatusOrderByUpdatedAtAsc(UserRequestStatus status);


}
