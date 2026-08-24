package ntnu.idi.mushroomidentificationbackend.repository;

import java.util.Date;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Reusable {@link Specification} predicates for querying {@link UserRequest} entities,
 * composed by the admin request table's status/date filters.
 */
public final class UserRequestSpecifications {

  private UserRequestSpecifications() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static Specification<UserRequest> statusIs(UserRequestStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  public static Specification<UserRequest> statusIsNot(UserRequestStatus status) {
    return (root, query, cb) -> cb.notEqual(root.get("status"), status);
  }

  public static Specification<UserRequest> createdBetween(Date from, Date to) {
    return (root, query, cb) -> cb.between(root.get("createdAt"), from, to);
  }
}
