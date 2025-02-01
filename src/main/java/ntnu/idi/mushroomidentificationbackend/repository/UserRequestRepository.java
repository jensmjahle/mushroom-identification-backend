package ntnu.idi.mushroomidentificationbackend.repository;

import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, String> {

  // Get paginated user requests by status sorted by updatedAt (ascending)
  Page<UserRequest> findByStatusOrderByUpdatedAtAsc(UserRequestStatus status, Pageable pageable);
  
  // Get paginated user requests for a specific admin by status, sorted by updatedAt (ascending)
  Page<UserRequest> findByAdminAndStatusOrderByUpdatedAtAsc(Admin admin, UserRequestStatus status, Pageable pageable);

}
