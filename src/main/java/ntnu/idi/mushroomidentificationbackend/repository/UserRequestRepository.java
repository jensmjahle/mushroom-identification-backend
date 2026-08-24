package ntnu.idi.mushroomidentificationbackend.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, String>,
    JpaSpecificationExecutor<UserRequest> {
  Optional<UserRequest> findFirstByStatusAndAdminIsNullOrderByUpdatedAtAsc(UserRequestStatus status);
  Optional<UserRequest> findByPasswordHash(String passwordHash);
  Optional<UserRequest> findByUserRequestId(String userRequestId);
  Optional<UserRequest> findByLookUpKey(String referenceCode);
  int deleteByCreatedAtBefore(Date dateThreshold);
  Long countByStatus(UserRequestStatus status);
  List<UserRequest> findByCreatedAtBetween(Date createdAt, Date createdAt2);
  long countByCreatedAtBetween(Date createdAt, Date createdAt2);
  long countByStatusAndCreatedAtBetween(UserRequestStatus status, Date createdAt, Date createdAt2);
  List<UserRequest> findByCreatedAtBefore(Date date);
}
